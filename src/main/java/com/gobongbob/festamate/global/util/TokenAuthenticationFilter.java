package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT를 검증하고 인증 정보를 설정하는 클래스
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    // 여기서 Refresh Token이 아닌 Access Token만 허용하도록 설정
    // 검증된 토큰으로 SecurityContextHolder에 인증 정보를 저장함

    // doFilterInternal 메서드는 JWT 없이 가능한 경로
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 공용 경로는 필터 통과
        if (isPublicPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 꺼내기
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        // 가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);

        // 토큰 유효성 검사
        if (token != null && tokenProvider.validateToken(token)) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            Object principal = authentication.getPrincipal();

            // CustomMemberDetails 타입인지 확인
            if (!(principal instanceof CustomMemberDetails)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 인증 실패 처리
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("/api/auth/kakao")
                || uri.startsWith("/api/auth/login")  // 기존 유저 로그인
                || uri.startsWith("/api/auth/register") // 회원가입(프로필 등록)
                || uri.equals("/health")
                || uri.equals("/sentry")
                || uri.equals("/error")
                || uri.equals("/api/rooms/list")
                || uri.startsWith("/login/oauth2/")
                || uri.startsWith("/test/") // `/test/`로 시작하는 모든 경로를 공용 경로로 추가
                || uri.startsWith("/api/auth/phone/") // 인증번호 요청 및 확인
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars/")
                || uri.equals("/swagger-ui.html");
    }
}
