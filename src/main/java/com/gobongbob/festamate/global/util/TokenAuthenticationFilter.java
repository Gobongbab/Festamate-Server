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

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

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
        String token = getAccessToken(authorizationHeader);

        // 토큰 유효성 검사
        if (token != null && tokenProvider.validateToken(token) &&
                (tokenProvider.isInitialAccessToken(token)
                        || tokenProvider.isFinalAccessToken(token)
                        || tokenProvider.isTestAccessToken(token))) {

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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
        return uri.equals("/api/auth/kakao")
                || uri.equals("/health")
                || uri.equals("/sentry")
                || uri.equals("/error")
                || uri.equals("/api/rooms/list")
                || uri.startsWith("/login/oauth2/");
    }
}
