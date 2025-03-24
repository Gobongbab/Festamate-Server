package com.gobongbob.festamate.domain.auth.oauth.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gobongbob.festamate.domain.auth.jwt.domain.RefreshJwtToken;
import com.gobongbob.festamate.domain.auth.jwt.persistence.RefreshTokenRepository;
import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.CookieUtil;
import com.gobongbob.festamate.global.util.OauthProvider;
import com.gobongbob.festamate.global.util.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// 인가 코드를 받아서 액세스 토큰을 요청하고, 사용자 정보를 가져오는 서비스로 카카오 로그인 페이지로 리다이렉션 됨.

@Service
@Transactional(readOnly = true)
public class OauthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private final String CLIENT_ID;
    private final String REDIRECT_URI;
    private final String ACCESS_HEADER;

    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Autowired
    public OauthService(MemberRepository memberRepository,
            TokenProvider tokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${KAKAO_CLIENT_ID}") String CLIENT_ID,
            @Value("${KAKAO_REDIRECT_URI}") String REDIRECT_URI,
            @Value("${jwt.header}") String ACCESS_HEADER) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.CLIENT_ID = CLIENT_ID;
        this.REDIRECT_URI = REDIRECT_URI;
        this.ACCESS_HEADER = ACCESS_HEADER;
    }

    // 인가 코드를 사용하여 액세스 토큰을 요청하고, 사용자 정보를 가져와 리턴함
    @Transactional
    public Map<String, String> kakaoLogin(String code, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // 1. 인가 코드로 OAuth2 액세스 토큰 요청
            String oauthAccessToken = getAccessToken(code);

            // 2. 카카오 서버로부터 받은 OAuth2 액세스 토큰으로 사용자 정보를 요청
            JsonNode userInfo = getKakaoUserInfo(oauthAccessToken);

            // 3. 사용자 정보 저장 또는 업데이트
            Member member = registerKakaoUser(userInfo, oauthAccessToken);

            // 4. JWT 생성 및 저장
            Map<String, String> tokens = generateJwtTokens(member, request, response);

            return tokens;

        } catch (Exception e) {
            throw new RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    private Map<String, String> generateJwtTokens(Member member, HttpServletRequest request,
            HttpServletResponse response) {
        // JWT 액세스 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(member);
        response.setHeader(ACCESS_HEADER, accessToken);

        // JWT 리프레시 토큰 생성 및 저장
        String refreshToken = tokenProvider.generateRefreshToken(member);
        saveRefreshToken(member.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        // 응답 데이터 구성
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response,
            String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addHttpOnlyCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken,
                cookieMaxAge);
    }

    private void saveRefreshToken(Long memberId, String newRefreshToken) {
        RefreshJwtToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshJwtToken(memberId, newRefreshToken));
        refreshTokenRepository.save(refreshToken);
    }

    // 인가 코드로 카카오 서버에 액세스 토큰을 요청
    private String getAccessToken(String code) {
        try {
            // HTTP Header 생성
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // HTTP Body 생성
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", CLIENT_ID);
            body.add("redirect_uri", REDIRECT_URI);
            body.add("code", code);

            // HTTP 요청 보내기
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body,
                    headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("카카오 액세스 토큰 요청 중 오류가 발생했습니다.", e);
        }
    }

    private JsonNode getKakaoUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    userInfoRequest,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 요청 중 오류가 발생했습니다.", e);
        }
    }

    // 카카오 회원 정보를 데이터베이스에 저장하는 메서드
    private Member registerKakaoUser(JsonNode userInfoJson, String kakaoAccessToken) {
        try {
            String oauthId = userInfoJson.get("id").asText();
            JsonNode profileJson = userInfoJson.get("kakao_account").get("profile");
            String nickname = profileJson.get("nickname").asText();

            OauthInfo oauthInfo = new OauthInfo(oauthId, OauthProvider.KAKAO);

            Member member = memberRepository.findByOauthInfo(oauthInfo)
                    .map(entity -> entity.update(kakaoAccessToken))
                    .orElse(Member.builder()
                            .kakaoAccessToken(kakaoAccessToken)
                            .nickname(nickname)
                            .oauthInfo(oauthInfo)
                            .build());

            return memberRepository.save(member);

        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 저장 중 오류가 발생했습니다.", e);
        }
    }
}
