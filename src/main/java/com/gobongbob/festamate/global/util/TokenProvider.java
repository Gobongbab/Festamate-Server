package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.member.domain.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secret;
    private final UserDetailsService userDetailsService;

    // 토큰 생성 (TokenType에 따라 다르게 생성)
    public Map<String, String> generateTokens(Member member, TokenType type) {
        String accessToken = createToken(member, "access", type.getDuration(),
                TokenType.FINAL_ACCESS);
        String refreshToken = createToken(member, "refresh", type.getDuration(),
                TokenType.FINAL_REFRESH);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    // Access Token 생성
    public String createAccessToken(Member member, TokenType type) {
        return createToken(member, "access", type.getDuration(), type);
    }

    // 기본 토큰 생성 로직
    private String createToken(Member member, String tokenType, Duration duration, TokenType type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + duration.toMillis());
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("id", member.getId())
                .claim("name", member.getName())
                .claim("nickname", member.getNickname())
                .claim("studentId", member.getStudentId())
                .claim("phoneNumber", member.getPhoneNumber())
                .claim("type", tokenType)
                .claim("memberType", type.name())
                .claim("role",
                        member.getRole().getAuthority()); // "ROLE_USER", "ROLE_ADMIN" 등 권한 문자열 추가

        if (member.getGender() != null) {
            builder.claim("gender", member.getGender().name());
        }
        if (member.getStudentDepartment() != null) {
            builder.claim("department", member.getStudentDepartment());
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // Refresh Token을 사용하여 새로운 Access Token을 생성할 때 사용
    public Long getMemberIdFromRefreshToken(String refreshToken) {
        return parseClaims(refreshToken).get("id", Long.class);
    }

    /**
     * 토큰의 유효성을 검증합니다. 유효하지 않은 경우 BadCredentialsException을 던져 JwtAuthenticationEntryPoint가 동작하도록 유도합니다.
     *
     * @param token 검증할 JWT 토큰
     * @throws BadCredentialsException 토큰이 유효하지 않을 때 (서명 오류, 만료, 형식 오류 등)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException 또는 IllegalArgumentException 발생 시
            // BadCredentialsException을 던져 인증 실패를 알림
            // 이 예외는 Spring Security의 ExceptionTranslationFilter에 의해 처리되어
            // 설정된 JwtAuthenticationEntryPoint의 commence 메소드를 호출하게 됨
            throw new BadCredentialsException("유효하지 않은 토큰입니다.", e); // 수정된 부분: BadCredentialsException 던지기
//            이 BadCredentialsException은 필터 밖으로 전파됩니다.
//            중요: 필터 내에서 이 예외를 잡아서 다른 처리를 하면 안 됩니다. 예외가 Spring Security의 기본 필터 체인으로 넘어가야 합니다.
//            Spring Security의 ExceptionTranslationFilter가 이 AuthenticationException을 감지합니다.(BadCredentialsException은 AuthenticationException의 하위 클래스)
//            ExceptionTranslationFilter는 설정된 AuthenticationEntryPoint (즉, 사용자가 만든 JwtAuthenticationEntryPoint)의 commence 메소드를 호출합니다.
//            JwtAuthenticationEntryPoint.commence() 메소드가 실행되어 response.sendError(HttpServletResponse.SC_UNAUTHORIZED)를 통해 클라이언트에게 401 에러를 반환합니다.
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.get("id").toString());
    }

    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
    }

    private String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }
}
