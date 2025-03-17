package com.gobongbob.festamate.global.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.gobongbob.festamate.global.util.JwtAccessDeniedHandler;
import com.gobongbob.festamate.global.util.JwtAuthenticationEntryPoint;
import com.gobongbob.festamate.global.util.TokenAuthenticationFilter;
import com.gobongbob.festamate.global.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider; // JWT 토큰을 생성하고 검증하는 역할

    @Bean
    public WebSecurityCustomizer configure() {
        return web -> web.ignoring()
                // 정적 리소스에 대한 스프링 시큐리티 사용을 비활성화
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 인증 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 기반 로그인 비활성화
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)) // 세션을 생성하지 않고, 토큰 기반 인증을 사용
                .authorizeHttpRequests(authorize -> authorize // 요청에 대한 인증 및 인가 설정 시작
                        .requestMatchers("/api/auth/**", "/login/oauth2/code/kakao")
                        .permitAll() // 경로에 대한 요청은 인증 없이 접근 가능
                        .anyRequest().authenticated() // 나머지 요청은 인증이 필요
                )
                .cors(withDefaults())
                .addFilterBefore(new TokenAuthenticationFilter(tokenProvider),
                        // JWT 토큰을 통해 인증된 사용자 정보 가져옴
                        UsernamePasswordAuthenticationFilter.class) // 헤더를 확인할 커스텀 필터 추가
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(
                                new JwtAuthenticationEntryPoint()) // 인증 실패 시 예외 처리
                        .accessDeniedHandler(new JwtAccessDeniedHandler())); // 인가 실패 시 예외 처리
        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }
}
