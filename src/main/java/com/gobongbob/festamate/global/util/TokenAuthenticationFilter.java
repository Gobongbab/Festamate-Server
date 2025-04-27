package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT를 검증하고 인증 정보를 설정하는 클래스
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 여기서 Refresh Token이 아닌 Access Token만 허용하도록 설정
    // 검증된 토큰으로 SecurityContextHolder에 인증 정보를 저장함

    // doFilterInternal 메서드는 JWT 없이 가능한 경로
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 공용 경로는 필터 통과
        if (isPublicPath(request)) {
            filterChain.doFilter(request, response); // Public 경로는 JWT 검증 없이 통과
            return;
        }

        // 토큰 꺼내기
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        // 가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);

        // 토큰 유효성 검사
        if (token != null) {
            if (!tokenProvider.validateToken(token)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"유효하지 않거나 만료된 토큰입니다.\"}");

                return;
            }

            Authentication authentication = tokenProvider.getAuthentication(token);
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomMemberDetails) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // principal 타입이 아니어도 그냥 인증 안 된 채로 통과시킴
        }

        // ✅ 인증 실패해도 그냥 다음 필터로 넘긴다
        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. 기존의 Public 경로 체크 (메서드 무관)
        if (uri.startsWith("/api/auth/kakao")
                || uri.startsWith("/api/auth/login")  // 기존 유저 로그인
                || uri.startsWith("/api/auth/register") // 회원가입(프로필 등록)
                || uri.equals("/health")
                || uri.equals("/sentry")
                || uri.equals("/error")
                || uri.startsWith("/login/oauth2/")
                || uri.startsWith("/test/") // `/test/`로 시작하는 모든 경로를 공용 경로로 추가
                || uri.startsWith("/api/auth/phone/") // 인증번호 요청 및 확인
                || uri.startsWith("/api/check/student-card") // OCR 학생증 인증
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/actuator/")
                || uri.startsWith("/webjars/")
                || uri.equals("/swagger-ui.html")) {
            return true;
        }

        // 2. 관리자 로그인 경로 체크
        if (uri.equals("/api/admin/login") && HttpMethod.POST.matches(method)) {
            return true;
        }

        // 3. HTTP 메서드(GET)를 고려해야 하는 Public 경로 체크
        if (HttpMethod.GET.matches(method)) {
            // GET /api/rooms (모임방 목록 조회) 경로 확인
            if (uri.equals("/api/rooms")) {
                return true;
            }
        }

        // 3. 위 조건들에 해당하지 않으면 Public 경로가 아님 (JWT 검증 필요)
        return false;
    }
}
