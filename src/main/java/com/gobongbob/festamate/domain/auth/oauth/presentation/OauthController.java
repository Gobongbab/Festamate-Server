package com.gobongbob.festamate.domain.auth.oauth.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.oauth.application.OauthService;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.KakaoLoginRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.LoginWithKakaoRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoCheckResponse;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// 소그인 핵심 로직
// 인가 코드를 받아 OauthService의 kakaoLogin 메서드를 호출하고, 인가 코드를 사용하여 액세스 토큰을 요청 후 사용자 정보를 가져와 처리함

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OauthController {

    private final OauthService oauthService;
    private final MemberService memberService;
    private final TokenService tokenService;

    // 1️⃣ 회원 여부 확인용 (카카오 인가코드로)
    @PostMapping("/kakao")
    public SuccessResponse<KakaoCheckResponse> kakaoCheck(
            @RequestBody @Valid KakaoLoginRequest kakaoLoginRequest) {

        KakaoCheckResponse response = oauthService.checkKakaoUser(kakaoLoginRequest.getCode());
        return new SuccessResponse<>(response);
    }

    // 2️⃣ 기존 회원 로그인 (카카오 access token으로)
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> loginWithKakao(
            @RequestBody LoginWithKakaoRequest request,
            HttpServletResponse response) {

        // OauthService는 내부적으로 Member 조회 후 TokenService를 호출하여 토큰 Map 반환 가정
        Map<String, String> tokens = oauthService.loginWithKakao(request.getKakaoAccessToken());
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken"); // OauthService로부터 리프레시 토큰 받아옴

        // 리프레시 토큰을 HttpOnly 쿠키로 설정
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 본문에는 액세스 토큰만 포함하는 DTO 반환
        AuthResponse authResponse = new AuthResponse(accessToken);
        return ResponseEntity.ok(new SuccessResponse<>(authResponse));
    }

    // 3️⃣ 신규 회원 프로필 등록 후 JWT 발급
    @PostMapping("/register/profile")
    @Transactional // 회원 저장과 토큰 생성을 한 트랜잭션으로 묶음 (DB 기준)
    public ResponseEntity<SuccessResponse<AuthResponse>> registerProfile(
            @RequestBody ProfileRegisterRequest request,
            HttpServletResponse response) {

        // 학번 중복 여부 확인
        memberService.checkStudentIdDuplication(request.studentId());

        // OauthService를 통해 회원 정보 DB에 저장 및 회원 ID 반환
        Long userId = oauthService.registerNewMember(request);

        // TokenService를 사용하여 JWT 토큰 생성 및 Redis 저장
        Map<String, String> tokens = tokenService.generateAndSaveTokens(userId,
                TokenType.FINAL_ACCESS);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken"); // 생성된 리프레시 토큰

        // 리프레시 토큰을 HttpOnly 쿠키로 설정
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()); // 응답 헤더에 쿠키 추가

        // 본문에는 액세스 토큰만 포함하는 DTO 반환
        AuthResponse authResponse = new AuthResponse(accessToken);
        return ResponseEntity.ok(new SuccessResponse<>(authResponse)); // SuccessResponse 래퍼 사용
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)          // JavaScript 접근 불가
                .secure(true)           // HTTPS 환경에서만 전송 (로컬 HTTP 테스트 시 임시 주석 처리 고려)
                .path("/")              // 전체 경로에서 쿠키 사용 가능
                .maxAge(Duration.ofDays(14)) // 쿠키 만료 시간 (Redis TTL과 일치 권장)
                .sameSite("None")      // 동일 출처 요청에만 쿠키 전송 (CSRF 방지)
                .build();
    }
}
