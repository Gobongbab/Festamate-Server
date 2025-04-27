package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.domain.member.domain.Role;
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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Bean // 비밀번호 암호화에 사용할 PasswordEncoder 빈 등록
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()

                        // Swagger UI 관련 경로 허용 추가
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/**"
                        ).permitAll()

                        // 비로그인 허용 경로
                        .requestMatchers(
                                "/api/auth/kakao",         // JWT 필요 없음
                                "/api/auth/login",         // JWT 필요 없음
                                "/api/auth/register/**",   // 회원가입 관련 전체 경로 (프로필 포함) JWT 필요 없음
                                "/health",
                                "/sentry",
                                "/error",
                                "/login/oauth2/**",
                                "/test/**",
                                "/api/auth/phone/**", // 인증번호 요청 및 확인
                                "/api/check/student-card" // OCR 학생증 인증
                        ).permitAll()

                        // 모임방 관련 경로 (HttpMethod 명시)
                        .requestMatchers(HttpMethod.GET, "/api/rooms")
                        .permitAll()       // 모임방 목록 조회 (GET) 허용
                        .requestMatchers(HttpMethod.GET, "/api/rooms/{rooms_id}")
                        .permitAll() // 모임방 상세 조회 (GET) 허용

                        // 관리자 API 경로 (HttpMethod 명시)
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()

                        // 관리자 API 경로 (ADMIN 권한 필요)
                        .requestMatchers("/api/admin/**")
                        .hasAuthority(Role.ADMIN.getAuthority()) // "ROLE_ADMIN" 권한 필요

                        // 로그인 + JWT 인증이 필요한 경로
                        .requestMatchers(
                                "/api/auth/members/profile", // 프로필 조회는 JWT 필요
                                "/api/rooms",
                                "/api/report/room/**"
                        ).authenticated()

                        // 나머지는 모두 인증 필요
                        .anyRequest().authenticated()
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
        configuration.setAllowedOrigins(List.of(
                "https://festamate-web.vercel.app",
                "http://localhost:5173",
                "https://www.festamate.shop"
        ));

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

        // CORS 캐싱 시간 설정 (초 단위)
        configuration.setMaxAge(3600L);

        // URL 기반으로 CORS 설정 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
