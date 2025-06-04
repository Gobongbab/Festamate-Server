package com.gobongbob.festamate.domain.auth.oauth.application;

import static com.gobongbob.festamate.global.response.ResponseCode.KAKAO_API_REQUEST_FAILED;
import static com.gobongbob.festamate.global.response.ResponseCode.KAKAO_RESPONSE_EMPTY;
import static com.gobongbob.festamate.global.response.ResponseCode.KAKAO_TOKEN_INVALID_OR_EXPIRED;
import static com.gobongbob.festamate.global.response.ResponseCode.KAKAO_USER_ID_NOT_FOUND;
import static com.gobongbob.festamate.global.response.ResponseCode.KAKAO_USER_INFO_PARSING_ERROR;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;
import static com.gobongbob.festamate.global.response.ResponseCode.PROFILE_REGISTRATION_REQUIRED;
import static com.gobongbob.festamate.global.response.ResponseCode.USER_ALREADY_EXISTS;
import static com.gobongbob.festamate.global.response.ResponseCode.USER_NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.KakaoUserInfo;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoCheckResponse;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoTokenResponse;
import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// 소그인 핵심 로직
// 인가 코드를 받아서 액세스 토큰을 요청하고, 사용자 정보를 가져오는 서비스로 카카오 로그인 페이지로 리다이렉션 됨.

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OauthService {

    private final WebClient webClient;
    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final ProfileImageRepository profileImageRepository;
    private static final String DEFAULT_PROFILE_IMAGE_NAME = "default_profile_image.png";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    /**
     * 1️⃣ 카카오 인가 코드로 access token 요청 후, 2️⃣ 해당 access token으로 사용자 정보를 조회해서 3️⃣ DB에 kakaoId가 존재하는지 여부를 반환
     */
    public KakaoCheckResponse checkKakaoUser(String code) {
        // 1. 카카오 인가 코드를 통해 access token 발급
        String kakaoAccessToken = getKakaoAccessToken(code).get("accessToken");

        // 2. access token으로 사용자 정보 요청 (KakaoUserInfo 객체 반환)
        KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken); // 캐스팅 제거

        // 3. DB에 해당 kakaoId가 존재하는지 여부 확인
        boolean isMember = memberRepository.existsByKakaoId(userInfo.getId());

        return new KakaoCheckResponse(isMember, kakaoAccessToken);
    }

    /**
     * 기존 회원 로그인 시, Kakao Access Token으로 kakaoId 추출 후 사용자 ID 반환
     */
    public Long findUserIdByKakaoToken(String kakaoAccessToken) {
        KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken); // 캐스팅 제거
        Member member = memberRepository.findByKakaoId(userInfo.getId())
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
        return member.getId();
    }

    /**
     * 카카오 정보 기반 로그인 처리 및 JWT 발급
     */
    public Map<String, String> loginWithKakao(String kakaoAccessToken) {
        Long userId = findUserIdByKakaoToken(kakaoAccessToken); // 사용자 ID 조회

        // 유저 정보 가져오기
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND));

        // 프로필 완료 여부 확인
        if (member.isProfileCompleted()) {
            // 프로필 등록이 완료된 경우 JWT 반환
            return tokenService.generateAndSaveTokens(userId, TokenType.FINAL_ACCESS);
        } else {
            // 프로필 등록이 안 된 경우 예외 처리
            throw new BadRequestException(PROFILE_REGISTRATION_REQUIRED);
        }
    }

    /**
     * 신규 회원 프로필 등록: Kakao ID 기반 회원 생성 및 프로필 정보 저장
     */
    @Transactional
    public Long registerNewMember(ProfileRegisterRequest request) {
        KakaoUserInfo userInfo = getKakaoUserInfo(request.kakaoAccessToken()); // 캐스팅 제거

        if (memberRepository.existsByKakaoId(userInfo.getId())) {
            throw new BadRequestException(USER_ALREADY_EXISTS);
        }

        // 새로운 Member 객체 생성
        Member newMember = Member.builder()
                .kakaoId(userInfo.getId())
                .build();

        // 요청에서 받은 데이터를 Member 객체에 세팅
        request.toEntity(newMember);  // nickname은 랜덤 생성

        // DB에서 기본 프로필 이미지 조회하여 기본 이미지 설정
        profileImageRepository.findByStoreName(DEFAULT_PROFILE_IMAGE_NAME) // 상수를 사용하여 조회
                .ifPresent(defaultImage -> {
                    // 테스트 코드에서 확인된 initializeProfileImage 메서드 사용
                    newMember.initializeProfileImage(defaultImage);
                    System.out.println("✅ 기본 프로필 이미지 설정됨 ✅: " + defaultImage.getId());
                });

        // 프로필 등록 완료 처리
        newMember.completeProfile();

        // DB에 저장
        Member savedMember = memberRepository.save(newMember);

        return savedMember.getId();
    }

    // 카카오 인가 코드로 Access Token 발급
    private Map<String, String> getKakaoAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        KakaoTokenResponse response = webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE
                        + ";charset=utf-8") // charset 추가
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                // 응답 상태 코드 에러 처리 (4xx, 5xx)
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    // 401 Unauthorized 경우 토큰 만료 가능성 있음
                                    if (clientResponse.statusCode().value() == 401) {
                                        return Mono.error(
                                                new BadRequestException(KAKAO_TOKEN_INVALID_OR_EXPIRED)
                                        );
                                    }
                                    return Mono.error(
                                            new BadRequestException(KAKAO_API_REQUEST_FAILED)
                                    );
                                })
                )
                .bodyToMono(KakaoTokenResponse.class)
                .block(); // 동기 처리

        // Null 체크 및 토큰 추출
        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("카카오 액세스 토큰을 가져오는데 실패했습니다.");
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", response.getAccessToken());
        return tokens;
    }

    // Kakao Access Token으로 사용자 정보 조회 (반환 타입을 KakaoUserInfo로 변경)
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        String json = webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                // 응답 상태 코드 에러 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    // 401 Unauthorized 경우 토큰 만료 가능성 있음
                                    if (clientResponse.statusCode().value() == 401) {
                                        return Mono.error(
                                                new BadRequestException(KAKAO_TOKEN_INVALID_OR_EXPIRED)
                                        );
                                    }
                                    return Mono.error(
                                            new BadRequestException(
                                                    KAKAO_API_REQUEST_FAILED));
                                }))
                .bodyToMono(String.class)
                .block(); // 동기 처리

        if (json == null) {
            throw new BadRequestException(KAKAO_RESPONSE_EMPTY);
        }

        try {
            // ObjectMapper를 사용하여 JSON 문자열을 KakaoUserInfo DTO 객체로 변환
            KakaoUserInfo userInfo = objectMapper.readValue(json, KakaoUserInfo.class);

            // 필수 정보인 ID가 파싱되었는지 확인
            if (userInfo == null || userInfo.getId() == null) {
                throw new BadRequestException(KAKAO_USER_ID_NOT_FOUND);
            }
            return userInfo;
        } catch (JsonProcessingException e) {
            throw new BadRequestException(KAKAO_USER_INFO_PARSING_ERROR);
        }
    }
}
