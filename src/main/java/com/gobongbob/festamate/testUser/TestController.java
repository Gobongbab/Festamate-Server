package com.gobongbob.festamate.testUser;

import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/create")
    // 반환 타입을 ResponseEntity로 변경하고, 본문에는 AuthResponseDto를 담도록 수정
    public ResponseEntity<SuccessResponse<AuthResponse>> createTestMember(
            HttpServletResponse response) {

        // 1. TestService 호출하여 토큰 Map 받기
        Map<String, String> tokens = testService.createTestMemberAndGetTokens(); // 반환 타입은 Map<String, String>

        // 2. Map에서 accessToken과 refreshToken 추출
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // 3. 리프레시 토큰 HttpOnly 쿠키 설정
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 4. 본문에는 액세스 토큰만 포함하는 DTO 반환 (TestTokens 사용 안 함)
        AuthResponse authResponse = new AuthResponse(accessToken);
        return ResponseEntity.ok(new SuccessResponse<>(authResponse));
    }

    // 쿠키 생성 유틸리티 메서드
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // 로컬 HTTP 테스트 시 임시 주석 처리 고려
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict")
                .build();
    }

//    테스트 관리자 비활성화 
//    @PostMapping("/create-admin")
//    public ResponseEntity<TestService.TestTokens> createAdminMember() {
//        // 테스트 관리자용 유저 생성 및 JWT 토큰 반환
//        TestService.TestTokens adminTokens = testService.createAdminMember();
//        return ResponseEntity.ok(adminTokens);
//    }
}
