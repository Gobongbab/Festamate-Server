package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

// 카카오 서버로부터 받은 액세스 토큰을 사용하여 자체 JWT 토큰을 생성함
@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final UserDetailsService userDetailsService;

    // 서명에 사용할 시크릿 키
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 공통적인 JWT 생성 메서드
     *
     * @param member   사용자 정보
     * @param type     토큰의 용도 구분 (ex. initial_access, final_refresh, admin_access 등)
     * @param duration 토큰 유효 기간
     * @param isAdmin  관리자 여부 (role 클레임 포함 여부 결정)
     * @param isTest   테스트 계정 여부 (필요시 처리 가능)
     * @return JWT 문자열
     */
    private String createToken(Member member, String type, Duration duration, boolean isAdmin,
            boolean isTest) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + duration.toMillis());

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)  // 헤더 typ: "JWT"
                .setIssuedAt(now)                               // 발급 시간
                .setExpiration(expiry)                          // 만료 시간
                .setSubject(String.valueOf(member.getId()))     // 서브젝트: 사용자 ID
                .claim("id", member.getId())
                .claim("name", member.getName())
                .claim("nickname", member.getNickname())
                .claim("studentId", member.getStudentId())
                .claim("phoneNumber", member.getPhoneNumber())
                .claim("type", type);                           // type: 토큰 용도

        // 선택적인 정보
        if (member.getGender() != null) {
            builder.claim("gender", member.getGender().name());
        }

        if (member.getMajor() != null) {
            builder.claim("major", member.getMajor().name());
        }

        // 관리자용 role 클레임 추가
        if (isAdmin) {
            builder.claim("role", member.getRole());
        }

        // 서명 후 토큰 문자열 반환
        return builder
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // ========== ✅ 일반 사용자용 토큰 ==========

    // 최초 로그인 후 임시로 발급되는 Access Token (ex. 프로필 미완성 시)
    public String generateInitialAccessToken(Member member) {
        return createToken(member, "initial_access", Duration.ofHours(2), false, false);
    }

    public String generateInitialRefreshToken(Member member) {
        return createToken(member, "initial_refresh", Duration.ofDays(7), false, false);
    }

    // 프로필 완성 후 최종 Access Token
    public String generateFinalAccessToken(Member member) {
        return createToken(member, "final_access", Duration.ofHours(2), false, false);
    }

    public String generateFinalRefreshToken(Member member) {
        return createToken(member, "final_refresh", Duration.ofDays(7), false, false);
    }

    // 임시 토큰 (짧은 시간동안만 유효, 예: 로그인 직후 유효성 검사 등)
    public String createTemporaryAccessToken(Member member) {
        return createToken(member, "temporary_access", Duration.ofMinutes(10), false, false);
    }

    // ========== 👩‍💼 관리자 전용 토큰 ==========

    public String generateAdminAccessToken(Member member) {
        return createToken(member, "admin_access", Duration.ofDays(100), true, false);
    }

    public String generateAdminRefreshToken(Member member) {
        return createToken(member, "admin_refresh", Duration.ofDays(100), true, false);
    }

    // ========== 🧪 테스트 유저 전용 토큰 ==========

    public String generateTestAccessToken(Member member) {
        return createToken(member, "test_access", Duration.ofDays(100), false, true);
    }

    public String generateTestRefreshToken(Member member) {
        return createToken(member, "test_refresh", Duration.ofDays(100), false, true);
    }

    // ========== ✅ JWT 검증 관련 ==========

    /**
     * JWT에서 Claims 추출
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰도 클레임은 반환
        }
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException |
                 IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long memberId) {
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long memberId) {
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Long getUserId(String token) {
        // 토큰에서 사용자 ID를 추출하는 로직을 구현
        // 예: JWT 토큰에서 클레임을 파싱하여 사용자 ID를 반환
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    public Authentication getAuthentication(String token) {
        // 토큰에서 사용자 정보 추출
        String username = getUsernameFromToken(token);

        // 사용자 정보를 기반으로 Authentication 객체 생성
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
    }

    private String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

}
