package com.gobongbob.festamate.domain.auth.jwt.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // === Logger 인스턴스 생성 ===
    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;

    // 리프레시 토큰(HttpOnly 쿠키)을 사용하여 새로운 액세스 토큰을 발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        // --- 로그 추가 1: 컨트롤러 진입 확인 ---
        log.info("POST /api/auth/refresh 요청 수신됨.");

        // 1. 쿠키 존재 여부 확인
        if (refreshTokenCookie == null) {
            // --- 로그 추가 2: 쿠키가 없는 경우 (WARN 레벨) ---
            log.warn("요청 헤더에 'refreshToken' 쿠키가 존재하지 않습니다.");
            return ResponseEntity.status(401)
                    .body("리프레시 토큰 쿠키가 없습니다. 다시 로그인해주세요.");
        }

        // --- 로그 추가 3: 쿠키 값 확인 (앞 5자리만) ---
        String cookiePrefix =
                (refreshTokenCookie.length() > 5) ? refreshTokenCookie.substring(0, 5) + "..."
                        : "N/A";
        log.debug("전달받은 'refreshToken' 쿠키 값(앞 5자리): {}", cookiePrefix);

        try {
            // --- 로그 추가 4: 서비스 호출 직전 ---
            log.debug("TokenService.regenerateAccessToken 호출 시도...");
            TokenService.TokenRefreshResult result = tokenService.regenerateAccessToken(
                    refreshTokenCookie);
            // --- 로그 추가 5: 서비스 호출 성공 ---
            log.info("TokenService.regenerateAccessToken 호출 성공. 새 토큰 생성 완료.");

            // 3. 성공 시: 새 리프레시 토큰 HttpOnly 쿠키 설정
            // (쿠키 설정 로그는 필요시 추가 가능하나, 여기서는 생략)
            ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken",
                            result.getRefreshToken()) // 서비스로부터 받은 새 리프레시 토큰 값 사용
                    .httpOnly(true)
                    .secure(true) // HTTPS 필수
                    .path("/")
                    .maxAge(Duration.ofDays(14))
                    .sameSite("None") // Cross-site 허용
                    .build();

            // 4. 응답 반환 (200 OK + 새 액세스 토큰 + 새 쿠키)
            log.info("액세스 토큰 재발급 성공. 200 OK 응답 및 새 쿠키 설정.");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                    .body(new AuthResponse(result.getAccessToken()));

        } catch (IllegalArgumentException e) {
            // --- 로그 추가 6: TokenService에서 발생한 예상된 예외 처리 (ERROR 레벨) ---
            // 이 경우는 TokenService 내부 로그(WARN)에서 이미 원인이 찍혔을 가능성이 높음
            log.error("액세스 토큰 재발급 실패 (TokenService 예외 발생): {}", e.getMessage());

            // 5. 실패 시: 401 Unauthorized 응답 및 쿠키 삭제 시도
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .path("/")
                    // secure, sameSite 등은 삭제 시 크게 중요하지 않을 수 있음
                    .build();

            log.warn("액세스 토큰 재발급 실패로 401 응답 및 쿠키 삭제 시도.");
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body("토큰 재발급에 실패했습니다. 다시 로그인해주세요. 이유: " + e.getMessage()); // 원인 메시지 포함 (선택)

        } catch (Exception e) {
            // --- 로그 추가 7: 예상치 못한 다른 모든 예외 처리 (ERROR 레벨 + 스택 트레이스) ---
            log.error("액세스 토큰 재발급 처리 중 예상치 못한 예외 발생!", e); // ⭐ 스택 트레이스 포함!

            // 예상치 못한 오류는 500 Internal Server Error 로 응답하는 것이 더 적절함
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("토큰 재발급 중 서버 오류가 발생했습니다.");
        }
    }

    // 로그아웃 컨트롤러 메서드
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("POST /api/auth/logout 요청 수신됨.");
        String userId = null; // 초기화

        try {
            // 1. 현재 인증된 사용자 ID 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                log.warn("로그아웃 요청: 인증된 사용자 정보를 찾을 수 없음.");
                // 인증 안 된 사용자도 로그아웃 요청은 허용하고 쿠키 삭제만 시도할 수 있음 (선택적)
                // 여기서는 일단 401 반환하도록 처리 (기존 로직 유지)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("로그아웃 요청 사용자를 식별할 수 없습니다.");
            }

            userId = extractUserIdFromPrincipal(authentication.getPrincipal());
            log.debug("로그아웃 요청 사용자 ID 추출 시도 결과: {}", userId);

            if (userId == null) {
                log.error("로그아웃 요청: 인증 정보는 있으나 사용자 ID 추출 실패. Principal: {}",
                        authentication.getPrincipal());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("로그아웃 처리 중 사용자 식별 오류 발생.");
            }

            // 2. Redis에서 리프레시 토큰 삭제 (TokenService 호출)
            log.debug("TokenService.deleteRefreshTokenFromRedis 호출 시도. 사용자 ID: {}", userId);
            tokenService.deleteRefreshTokenFromRedis(userId);
            log.info("Redis에서 리프레시 토큰 삭제 처리 완료 (또는 시도 완료). 사용자 ID: {}", userId);

            // 3. 클라이언트 쿠키 삭제 지시 (응답 헤더 설정)
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .path("/")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
            log.debug("클라이언트 측 refreshToken 쿠키 삭제 헤더 설정 완료.");

            // 4. 성공 응답 (UTF-8 인코딩 헤더 추가)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.valueOf(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));

            log.info("로그아웃 처리 성공. 사용자 ID: {}", userId);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body("로그아웃 처리되었습니다.");

        } catch (Exception e) {
            // --- 로그 추가 (로그아웃 실패 시) ---
            log.error("로그아웃 처리 중 예상치 못한 예외 발생! 사용자 ID (추출 시도): {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그아웃 처리 중 서버 오류가 발생했습니다.");
        }
    }

    // 사용자 ID 추출 헬퍼 메서드 (내부 로직은 동일, 로그 추가 불필요해 보임)
    private String extractUserIdFromPrincipal(Object principal) {
        if (principal instanceof CustomMemberDetails) {
            Long longId = ((CustomMemberDetails) principal).getMember().getId();
            if (longId != null) {
                return String.valueOf(longId);
            }
        } else if (principal instanceof String) {
            // String 타입 Principal이 ID인 경우도 고려 (예: OAuth)
            // 다만, 숫자 형태인지 정도만 간단히 확인하거나, 시스템 설계에 따라 조정 필요
            // 여기서는 일단 반환 (TokenService에서 Long.parseLong으로 검증하므로)
            return (String) principal;
        }
        // 다른 타입의 Principal은 로그 등으로 확인 후 추가 지원 필요
        log.warn("알 수 없는 Principal 타입으로 사용자 ID 추출 시도: {}", principal.getClass().getName());
        return null;
    }
}
