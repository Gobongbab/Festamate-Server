package com.gobongbob.festamate.global.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT를 검증하고 인증 정보를 설정하는 클래스
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    // 여기서 Refresh Token이 아닌 Access Token만 허용하도록 설정
    // 검증된 토큰으로 SecurityContextHolder에 인증 정보를 저장함
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        /** 테스트를 위해 임시 비활성화
         String requestUri = request.getRequestURI();

         // 로그인, 카카오 및 리프레시, 닉네임 중복 확인 엔드포인트는 필터를 건너뜀
         if ("/api/auth/login".equals(requestUri) || "/api/auth/refresh".equals(requestUri)
         || "/login/oauth2/code/kakao".equals(requestUri)
         || "/api/auth/register/check/nickname".equals(requestUri)) {
         filterChain.doFilter(request, response);
         return;
         }

         // 요청 헤더의 Authorization 키의 값 조회
         String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
         // 가져온 값에서 접두사 제거
         String token = getAccessToken(authorizationHeader);
         // 가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정
         if (token != null && tokenProvider.validateToken(token) && tokenProvider.isAccessToken(
         token)) {
         Authentication authentication = tokenProvider.getAuthentication(token);
         SecurityContextHolder.getContext().setAuthentication(authentication);
         } else {
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 인증 실패 처리
         return;
         }*/

        filterChain.doFilter(request, response);
    }

    /** 테스트를 위해 임시 비활성화
     private String getAccessToken(String authorizationHeader) {
     if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
     return authorizationHeader.substring(TOKEN_PREFIX.length());
     }
     return null;
     }*/
}
