package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

// 카카오 서버로부터 받은 액세스 토큰을 사용하여 자체 JWT 토큰을 생성함
@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final String header = "Authorization";

    @Value("${JWT_SECRET}")
    private String secret;

    // Access & Refresh Token 생성 메서드들
    public String generateInitialAccessToken(Member member) {
        return makeUserToken(new Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis()),
                member, "initial_access");
    }

    public String generateInitialRefreshToken(Member member) {
        return makeUserToken(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()),
                member, "initial_refresh");
    }

    public String generateFinalAccessToken(Member member) {
        return makeUserToken(new Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis()),
                member, "final_access");
    }

    public String generateFinalRefreshToken(Member member) {
        return makeUserToken(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()),
                member, "final_refresh");
    }

    public String generateTestAccessToken(Member member) {
        return makeUserToken(new Date(System.currentTimeMillis() + Duration.ofDays(100).toMillis()),
                member, "test_access");
    }

    public String generateTestRefreshToken(Member member) {
        return makeUserToken(new Date(System.currentTimeMillis() + Duration.ofDays(100).toMillis()),
                member, "test_refresh");
    }

    // 관리자용 Access Token 생성 메서드
    public String generateAdminAccessToken(Member member) {
        return makeAdminUserToken(
                new Date(System.currentTimeMillis() + Duration.ofDays(100).toMillis()),
                member, "admin_access");
    }

    // 관리자용 Refresh Token 생성 메서드
    public String generateAdminRefreshToken(Member member) {
        return makeAdminUserToken(
                new Date(System.currentTimeMillis() + Duration.ofDays(100).toMillis()),
                member, "admin_refresh");
    }

    // 공통 토큰 생성 로직 (initial, final, test 통합)
    private String makeUserToken(Date expiry, Member member, String type) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("id", member.getId())
                .claim("name", member.getName())
                .claim("nickname", member.getNickname())
                .claim("studentId", member.getStudentId())
                .claim("phoneNumber", member.getPhoneNumber())
                .claim("gender", member.getGender() != null ? member.getGender().name() : null)
                .claim("major", member.getMajor() != null ? member.getMajor().name() : null)
                .claim("type", type)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    private String makeAdminUserToken(Date expiry, Member member, String type) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("id", member.getId())
                .claim("name", member.getName())
                .claim("nickname", member.getNickname())
                .claim("studentId", member.getStudentId())
                .claim("phoneNumber", member.getPhoneNumber())
                .claim("role", member.getRole())
                .claim("gender", member.getGender() != null ? member.getGender().name() : null)
                .claim("major", member.getMajor() != null ? member.getMajor().name() : null)
                .claim("type", type)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 인증 객체 생성: 무조건 CustomMemberDetails로 반환
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        CustomMemberDetails userDetails = getCustomMemberDetailsFromClaims(claims);

        if (userDetails == null) {
            throw new IllegalArgumentException("Invalid token: UserDetails is null");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, token,
                userDetails.getAuthorities());
    }

    // 클레임 기반 CustomMemberDetails 생성
    private CustomMemberDetails getCustomMemberDetailsFromClaims(Claims claims) {
        Long id = claims.get("id", Long.class);
        if (id == null) {
            return null;
        }

        Member.MemberBuilder memberBuilder = Member.builder().id(id);
        String name = claims.get("name", String.class);
        String nickname = claims.get("nickname", String.class);
        String studentId = claims.get("studentId", String.class);
        String phoneNumber = claims.get("phoneNumber", String.class);
        String gender = claims.get("gender", String.class);
        String major = claims.get("major", String.class);
        String role = claims.get("role", String.class);

        if (name != null) {
            memberBuilder.name(name);
        }
        if (nickname != null) {
            memberBuilder.nickname(nickname);
        }
        if (studentId != null) {
            memberBuilder.studentId(studentId);
        }
        if (phoneNumber != null) {
            memberBuilder.phoneNumber(phoneNumber);
        }
        if (gender != null) {
            memberBuilder.gender(Gender.valueOf(gender));
        }
        if (major != null) {
            memberBuilder.major(Major.valueOf(major));
        }
        if (role != null) {
            memberBuilder.role(role);
        }

        return new CustomMemberDetails(memberBuilder.build());
    }

    // 유저 ID 추출
    public Long getUserId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // 토큰에서 Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // 타입 체크 메서드들
    public boolean isInitialAccessToken(String token) {
        return "initial_access".equals(getClaims(token).get("type"));
    }

    public boolean isInitialRefreshToken(String token) {
        return "initial_refresh".equals(getClaims(token).get("type"));
    }

    public boolean isFinalAccessToken(String token) {
        return "final_access".equals(getClaims(token).get("type"));
    }

    public boolean isFinalRefreshToken(String token) {
        return "final_refresh".equals(getClaims(token).get("type"));
    }

    public boolean isTestAccessToken(String token) {
        return "test_access".equals(getClaims(token).get("type"));
    }

    public boolean isTestRefreshToken(String token) {
        return "test_refresh".equals(getClaims(token).get("type"));
    }

    // 관리자 Access Token인지 확인하는 메서드
    public boolean isAdminAccessToken(String token) {
        Claims claims = getClaims(token);
        return "admin_access".equals(claims.get("type"));
    }

    // 관리자 Refresh Token인지 확인하는 메서드
    public boolean isAdminRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "admin_refresh".equals(claims.get("type"));
    }
}
