package com.gobongbob.festamate.domain.auth.jwt.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// JWT 토큰 재발급 관련 API 요청을 처리하는 컨트롤러
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/auth")
public class TokenController {

    private final TokenService tokenService;

    // 리프레시 토큰(HttpOnly 쿠키)을 사용하여 새로운 액세스 토큰을 발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        // 1. 쿠키 존재 여부 확인
        if (refreshTokenCookie == null) {
            // 리프레시 토큰 쿠키 없음 -> 재로그인 필요
            return ResponseEntity.status(401)
                    .body("리프레시 토큰 쿠키가 없습니다. 다시 로그인해주세요.");
        }

        try {
            // 2. TokenService 호출하여 토큰 재발급 시도 (RTR 포함)
            TokenService.TokenRefreshResult result = tokenService.regenerateAccessToken(
                    refreshTokenCookie);

            // 3. 성공 시: 새 리프레시 토큰 HttpOnly 쿠키 설정
            Duration refreshTokenValidity = TokenType.FINAL_REFRESH.getDuration(); // 6개월(180일)
            ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken",
                            result.getRefreshToken()) // 서비스로부터 받은 새 리프레시 토큰 값 사용
                    .httpOnly(true)          // JavaScript 접근 불가
                    .secure(true)           // HTTPS 환경에서만 전송 (로컬 HTTP 테스트 시 임시 주석 처리 고려)
                    .path("/")              // 전체 경로에서 쿠키 사용 가능
                    .maxAge(refreshTokenValidity) // 쿠키 만료 시간 (Redis TTL과 일치 권장)
                    .sameSite("None")      // 동일 출처 요청에만 쿠키 전송 (CSRF 방지)
                    .build();

            // 4. 응답 반환 (200 OK + 새 액세스 토큰 + 새 쿠키)
            return ResponseEntity.ok() // 200 OK
                    .header(HttpHeaders.SET_COOKIE,
                            newRefreshTokenCookie.toString()) // 응답 헤더에 새 쿠키 설정
                    .body(new AuthResponse(result.getAccessToken())); // 응답 본문에는 새 액세스 토큰만 포함

        } catch (IllegalArgumentException e) {
            // 5. 실패 시 (TokenService에서 예외 발생): 401 Unauthorized 응답 및 쿠키 삭제 시도
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0) // 만료 시간을 0으로 설정하여 즉시 삭제 유도
                    .path("/")
                    .build();

            return ResponseEntity.status(401) // 401 Unauthorized
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString()) // 응답 헤더에 쿠키 삭제 설정
                    .body("토큰 재발급에 실패했습니다. 다시 로그인해주세요.");
        }
    }

    // 로그아웃 컨트롤러 메서드
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 1. 현재 인증된 사용자 ID 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = extractUserIdFromPrincipal(
                authentication.getPrincipal());

        if (userId == null) {
            // 사용자 식별 불가 시 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그아웃 요청 사용자를 식별할 수 없습니다.");
        }

        // 2. Redis에서 리프레시 토큰 삭제 (TokenService 호출)
        tokenService.deleteRefreshTokenFromRedis(userId);

        // 3. 클라이언트 쿠키 삭제 지시 (응답 헤더 설정)
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));

        // 4. 성공 응답 (수정된 부분: headers 포함)
        return ResponseEntity.status(HttpStatus.OK) // 또는 ResponseEntity.ok()
                .headers(headers) // 생성한 헤더 객체를 명시적으로 포함
                .body("로그아웃 처리되었습니다.");
    }

    // 사용자 ID 추출 헬퍼 메서드
    private String extractUserIdFromPrincipal(Object principal) {
        if (principal instanceof CustomMemberDetails) {
            Long longId = ((CustomMemberDetails) principal).getMember().getId();
            if (longId != null) {
                return String.valueOf(longId); // Long ID를 String으로 변환하여 반환
            }
        } else if (principal instanceof String) {
            try {
                Long.parseLong((String) principal); // 숫자 변환 가능한지 체크
                return (String) principal;
            } catch (NumberFormatException e) {
            }
        } else if (principal instanceof Long) {
            // Principal 자체가 Long ID인 경우
            return String.valueOf(principal);
        }
        return null; // 추출 불가 시 null 반환
    }
}
