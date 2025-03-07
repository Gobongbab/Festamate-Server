package com.gobongbob.festamate.domain.image.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class OcrController {

    private static final Logger log = LoggerFactory.getLogger(OcrController.class);
    @Value("${naver.ocr.api.url}")
    private String ocrApiUrl;

    @Value("${naver.ocr.api.secret}")
    private String ocrApiSecret;


    @PostMapping("/api/check/student-card")
    public String performOcr(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 임시 파일로 저장
        Path tempFile = Files.createTempFile("tempImage", file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        // OCR API 호출
        String result = callOcrApi(tempFile.toFile());


        String studentName = getNameAfterKeyword(result, "성명");
        String studentId = getNameAfterKeyword(result, "학번");

        log.warn("studentName ->" + studentName);
        log.warn("studentId ->" + studentId);
        // 임시 파일 삭제
        Files.delete(tempFile);

        return result;
    }

    private String getNameAfterKeyword(String json, String keyword) {
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-OCR-SECRET", ocrApiSecret);

        // Get the filename from the file
        String filename = file.getName();

        // Create message with UUID, current timestamp and original filename
        String message = String.format(
                "{\"version\": \"v1\", \"requestId\": \"%s\", \"timestamp\": %d, \"lang\": \"ko\", \"images\": [{\"format\": \"jpg\", \"name\": \"%s\"}]}",
                java.util.UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                filename
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("message", message);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(ocrApiUrl, requestEntity, String.class);

        return result;
    }
}
