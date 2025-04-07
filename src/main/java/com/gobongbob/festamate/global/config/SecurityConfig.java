package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.global.util.JwtAccessDeniedHandler;
import com.gobongbob.festamate.global.util.JwtAuthenticationEntryPoint;
import com.gobongbob.festamate.global.util.TokenAuthenticationFilter;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 참조
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 인증 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 기반 로그인 비활성화
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)) // 세션을 생성하지 않고, 토큰 기반 인증을 사용
                .authorizeHttpRequests(authorize -> authorize // 요청에 대한 인증 및 인가 설정 시작
                        .requestMatchers(CorsUtils::isPreFlightRequest)
                        .permitAll() // Preflight 요청 허용 (OPTIONS 메서드)
                        .anyRequest().permitAll() // 모든 요청 허용
                )
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트 주소 명시 (credentials: true와 함께 쓰기 위해 * 안 됨)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 사용할 HTTP 메서드 명시
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(Arrays.asList(
                "X-Requested-With",
                "Content-Type",
                "Authorization",
                "X-XSRF-token",
                "Accept"
        ));

        // 인증 정보 포함 여부
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // CORS 캐싱 시간 설정 (초 단위)
        configuration.setMaxAge(3600L);

        // URL 기반으로 CORS 설정 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
