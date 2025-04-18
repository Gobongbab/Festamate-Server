package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
        String accessToken = createToken(member, "access", TokenType.FINAL_ACCESS.getDuration(),
                TokenType.FINAL_ACCESS);
        String refreshToken = createToken(member, "refresh", TokenType.FINAL_REFRESH.getDuration(),
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
                .claim("memberType", type.name());

        if (member.getGender() != null) {
            builder.claim("gender", member.getGender().name());
        }
        if (member.getStudentDepartment() != null) {
            builder.claim("department", member.getStudentDepartment());
        }
        if (type.isAdmin()) {
            builder.claim("role", member.getRole());
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // Refresh Token을 사용하여 새로운 Access Token을 생성할 때 사용
    public Long getMemberIdFromRefreshToken(String refreshToken) {
        return parseClaims(refreshToken).get("id", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
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
