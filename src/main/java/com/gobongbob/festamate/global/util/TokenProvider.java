package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

// 카카오 서버로부터 받은 액세스 토큰을 사용하여 자체 JWT 토큰을 생성함
@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final String header = "Authorization";

    @Value("${JWT_SECRET}")
    private String secret;

    // Access Token 생성 메서드
    public String generateAccessToken(Member member) {
        return makeToken(new Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis()),
                member, "access");
    }

    // Refresh Token 생성 메서드
    public String generateRefreshToken(Member member) {
        return makeToken(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()),
                member, "refresh");
    }

    // JWT 토큰을 실제로 생성하는 내부 메서드로, 토큰의 헤더, 페이로드, 서명을 설정함

    /***
     * 헤더 typ(타입) : JWT
     * 내용 iat(발급 일시) : 현재 시간
     * 내용 exp(만료일시) : expiry 멤버 변수값
     * 내용 sub(토큰 제목) : 회원 ID
     * 클레임 id : 회원 ID
     * 서명 : 비밀값과 함께 해시값을 HS256 방식으로 암호화
     */
    private String makeToken(Date expiry, Member member, String type) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("id", member.getId())
                .claim("type", type) // 타입 추가
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // JWT 토큰의 유효성을 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secret)   // 비밀값으로 복호화
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) { // 복호화 과정에서 에러가 나면 유효하지 않은 토큰임
            return false;
        }
    }

    // 토큰 기반으로 인증 정보를 가져오는 메서드
    // JWT 토큰에서 사용자 정보를 추출하여 Authentication 객체를 생성하며, 이를 통해 @AuthenticationPrincipal을 사용할 수 있음
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(
                new Member(claims.getSubject(), "", authorities), token, authorities);
    }

    // 토큰에서 회원 ID를 추출함
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    // 주어진 토큰에서 클레임을 추출함
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 클레임에서 토큰 타입을 확인하여 access token인지 검증하는 로직 추가
    public boolean isAccessToken(String token) {
        Claims claims = getClaims(token);
        return "access".equals(claims.get("type")); // 타입 검증
    }

    // Refresh Token인지 확인하는 메서드
    public boolean isRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "refresh".equals(claims.get("type")); // 타입 검증
    }
}
