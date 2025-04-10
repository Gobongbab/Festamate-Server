package com.gobongbob.festamate.domain.auth.oauth.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.KakaoUserInfo;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoCheckResponse;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoTokenResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

// 소그인 핵심 로직
// 인가 코드를 받아서 액세스 토큰을 요청하고, 사용자 정보를 가져오는 서비스로 카카오 로그인 페이지로 리다이렉션 됨.

@Service
@RequiredArgsConstructor
public class OauthService {

    private final WebClient webClient;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    /**
     * 1️⃣ 카카오 인가 코드로 access token 요청 후, 2️⃣ 해당 access token으로 사용자 정보를 조회해서 3️⃣ DB에 kakaoId가 존재하는지
     * 여부를 반환
     */
    public KakaoCheckResponse checkKakaoUser(String code) {
        // 1. 카카오 인가 코드를 통해 access token 발급
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 2. access token으로 사용자 정보 요청
        KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken);

        // 3. DB에 해당 kakaoId가 존재하는지 여부 확인
        boolean isMember = memberRepository.existsByKakaoId(userInfo.getId());

        return new KakaoCheckResponse(isMember, kakaoAccessToken);
    }

    /**
     * 기존 회원 로그인 시, Kakao Access Token으로 kakaoId 추출 후 유저 ID 반환
     */
    public Long findUserIdByKakaoToken(String kakaoAccessToken) {
        KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken);
        Member member = memberRepository.findByKakaoId(userInfo.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return member.getId();
    }

    /**
     * 신규 회원 프로필 등록: Kakao ID 기반 회원 생성
     */
    @Transactional
    public Long registerNewMember(ProfileRegisterRequest request) {
        KakaoUserInfo userInfo = getKakaoUserInfo(request.kakaoAccessToken()); // record의 필드 사용

        if (memberRepository.existsByKakaoId(userInfo.getId())) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        // 새로운 Member 객체 생성
        Member newMember = Member.builder()
                .kakaoId(userInfo.getId())
                .build();

        // 요청에서 받은 데이터를 Member 객체에 세팅
        request.toEntity(newMember);  // nickname은 랜덤 생성

        // 프로필 등록이 완료되었음을 표시
        newMember.completeProfile(); // 프로필 완료 처리

        // RDS에 저장
        memberRepository.save(newMember);  // 이 부분 추가!

        // 성공적으로 저장된 Member의 ID 반환
        return newMember.getId();
    }

    // 🔹 카카오 인가 코드로 Access Token 발급
    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        KakaoTokenResponse response = webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(body)) // ✅ 꼭 이렇게!
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();

        return response.getAccessToken();
    }

    // 🔹 Kakao Access Token으로 사용자 정보 조회
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        String json = webClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(json);
            Long kakaoId = root.get("id").asLong();
            return new KakaoUserInfo(kakaoId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }
}
