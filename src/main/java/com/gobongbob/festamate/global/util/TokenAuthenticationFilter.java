package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.domain.MinimalMemberDetails;
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
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    // 여기서 Refresh Token이 아닌 Access Token만 허용하도록 설정
    // 검증된 토큰으로 SecurityContextHolder에 인증 정보를 저장함

    // doFilterInternal 메서드는 JWT 없이 가능한 경로
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/test/")
                || "/login/oauth2/code/kakao".equals(requestUri)
                || requestUri.startsWith("/api/auth/") && !"/api/auth/register/profile".equals(
                requestUri)
                || "/health".equals(requestUri) || "/sentry".equals(requestUri) || "/error".equals(
                requestUri) || "/api/".equals(requestUri) || "/api/rooms/list".equals(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        // 가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);
        // 가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정
        if (token != null && tokenProvider.validateToken(token) &&
                (tokenProvider.isInitialAccessToken(token) || tokenProvider.isFinalAccessToken(
                        token) || tokenProvider.isTestAccessToken(
                        token))) { // 테스트용 Access Token도 검증
            Authentication authentication = tokenProvider.getAuthentication(token);
            Object principal = authentication.getPrincipal();
            if (principal instanceof MinimalMemberDetails
                    || principal instanceof CustomMemberDetails) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 인증 실패 처리
                return;
            }
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
}
