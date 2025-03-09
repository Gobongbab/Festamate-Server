package com.gobongbob.festamate.domain.image.application;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gobongbob.festamate.domain.image.dto.response.StudentInfoResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Transactional
@RequiredArgsConstructor
public class OcrService {

    @Value("${naver.ocr.api.url}")
    private String ocrApiUrl;

    @Value("${naver.ocr.api.secret}")
    private String ocrApiSecret;

    private final MemberRepository memberRepository;

    public StudentInfoResponse checkStudentCard(MultipartFile file, Long memberId) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 파일이 비어있거나 null인 경우 예외 처리
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 임시 파일로 저장
        Path tempFile = Files.createTempFile("tempImage", file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        // OCR API 호출
        String result = callOcrApi(tempFile.toFile());

        // JSON 파싱
        String studentName = getValueAfterKeyword(result, "성명");
        String studentDepartment = getValueAfterKeyword(result, "학과");
        String studentId = getValueAfterKeyword(result, "학번");

        // 임시 파일 삭제
        Files.delete(tempFile);

        member.setStudentInfo(studentName, studentDepartment, studentId);
        memberRepository.save(member);

        return StudentInfoResponse.fromEntity(studentName, studentDepartment, studentId);
    }
    private String getValueAfterKeyword(String json, String keyword) {
        try {
            // ObjectMapper는 JSON 데이터를 Java 객체로 변환하거나, Java 객체를 JSON으로 변환하는 기능을 제공
            ObjectMapper mapper = new ObjectMapper();
            // 입력된 JSON 문자열(json)을 파싱하여 트리 형태의 데이터 구조(JsonNode)로 변환합니다.
            JsonNode rootNode = mapper.readTree(json);

            /**
             * 예시
             * {
             *   "name": "홍길동",
             *   "age": 25,
             *   "address": {
             *     "city": "서울",
             *     "zipcode": "12345"
             *   }
             * }
             * 위 json을 트리 형태의 데이터 구조(JsonNode)로 변환하면 아래처럼 됨
             * rootNode
             * ├── name: "홍길동"
             * ├── age: 25
             * └── address
             *     ├── city: "서울"
             *     └── zipcode: "12345"
             *
             * 다음과 같이 접근 가능
             * String name = rootNode.path("name").asText(); // 홍길동
             * int age = rootNode.path("age").asInt(); // 25
             * String city = rootNode.path("address").path("city").asText(); // 서울
             *
             * */

            // "images" 배열에서 첫 번째 요소를 가져옴
            JsonNode fields = rootNode.path("images").get(0).path("fields");

            // "성명" 다음에 오는 inferText 값을 찾기 위한 반복문
            boolean foundKeyword = false;
            for (JsonNode field : fields) {
                String inferText = field.path("inferText").asText();

                if (foundKeyword) {
                    // 키워드 다음의 inferText를 찾았으므로 반환
                    return inferText;
                }

                if (keyword.equals(inferText)) {
                    // 키워드를 찾았으므로 플래그를 true로 설정
                    foundKeyword = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 찾지 못했을 경우 null 반환
        return null;
    }

    private String callOcrApi(File file) {
        // OCR API InvokeURL과 Secret Key를 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-OCR-SECRET", ocrApiSecret);

        // 파일로부터 파일 이름을 가져옴
        String filename = file.getName();

        // uuid를 만들고 timestamp를 현재 시간으로 설정
        String message = String.format(
                "{\"version\": \"v1\", \"requestId\": \"%s\", \"timestamp\": %d, \"lang\": \"ko\", \"images\": [{\"format\": \"jpg\", \"name\": \"%s\"}]}",
                java.util.UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                filename
        );

        // RestTemplate을 사용하여 multipart/form-data 형식으로 파일과 메시지를 전송
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("message", message);

        // RestTemplate을 사용하여 POST 요청을 보낼 때, HttpEntity를 사용하여 헤더와 바디를 설정
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // RestTemplate을 사용하여 POST 요청을 보냄
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(ocrApiUrl, requestEntity, String.class);

        return result;
    }
}
