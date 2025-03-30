package com.gobongbob.festamate.global.util;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.domain.MinimalMemberDetails;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

// 카카오 서버로부터 받은 액세스 토큰을 사용하여 자체 JWT 토큰을 생성함
@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final String header = "Authorization";

    @Value("${JWT_SECRET}")
    private String secret;

    // 초기 Access Token 생성 메서드 (카카오 로그인 직후)
    public String generateInitialAccessToken(Member member) {
        return makeInitialUserToken(
                new Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis()), member,
                "initial_access");
    }

    // 초기 Refresh Token 생성 메서드
    public String generateInitialRefreshToken(Member member) {
        return makeInitialUserToken(
                new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()),
                member, "initial_refresh");
    }

    // 최종 Access Token 생성 메서드 (사용자 등록 완료 후)
    public String generateFinalAccessToken(Member member) {
        return makeFinalUserToken(
                new Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis()), member,
                "final_access");
    }

    // 최종 Refresh Token 생성 메서드
    public String generateFinalRefreshToken(Member member) {
        return makeFinalUserToken(
                new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()),
                member, "final_refresh");
    }

    // 테스트용 Access Token 생성 메서드
    public String generateTestAccessToken(Member member) {
        return makeTestUserToken(
                new Date(System.currentTimeMillis() + Duration.ofDays(100).toMillis()),
                member, "test_access");
    }

    // 테스트용 Refresh Token 생성 메서드
    public String generateTestRefreshToken(Member member) {
        return makeTestUserToken(
                new Date(System.currentTimeMillis() + Duration.ofDays(100).toMillis()),
                member, "test_refresh");
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

    // 초기 유저용 토큰 생성 메서드
    private String makeInitialUserToken(Date expiry, Member member, String type) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("id", member.getId())
                .claim("type", type)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 최종 유저용 토큰 생성 메서드
    private String makeFinalUserToken(Date expiry, Member member, String type) {
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
                .claim("gender", member.getGender().name())
                .claim("major", member.getMajor().name())
                .claim("type", type)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 테스트 유저용 토큰 생성 메서드
    private String makeTestUserToken(Date expiry, Member member, String type) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("id", member.getId())
                .claim("name", member.getName()) // 추가
                .claim("nickname", member.getNickname()) // 추가
                .claim("studentId", member.getStudentId()) // 추가
                .claim("phoneNumber", member.getPhoneNumber()) // 추가
                .claim("gender", member.getGender().name()) // 추가
                .claim("major", member.getMajor().name()) // 추가
                .claim("type", type)
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
    // claims에서 필요한 정보를 추출하여 CustomMemberDetails 객체를 생성
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        UserDetails userDetails;

        if (isInitialAccessToken(token)) {
            MinimalMemberDetails minimalMemberDetails = minimalMemberDetails(claims);
            if (minimalMemberDetails == null) {
                throw new IllegalArgumentException("Invalid token: MinimalMemberDetails is null");
            }
            userDetails = minimalMemberDetails;
        } else {
            userDetails = getCustomMemberDetailsFromClaims(claims);
        }

        if (userDetails == null) {
            throw new IllegalArgumentException("Invalid token: UserDetails is null");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, token,
                userDetails.getAuthorities());
    }


    // 클레임에서 MinimalMemberDetails 객체를 생성하는 메서드(초기 정보 처리)
    private MinimalMemberDetails minimalMemberDetails(Claims claims) {
        Long id = claims.get("id", Long.class);
        if (id == null) {
            return null;
        }

        Member.MemberBuilder memberBuilder = Member.builder().id(id);
        Member member = memberBuilder.build();
        return new MinimalMemberDetails(member);
    }

    // 최종 정보 처리
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

        Member member = memberBuilder.build();
        return new CustomMemberDetails(member);
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

    // 초기 Access Token인지 확인하는 메서드
    public boolean isInitialAccessToken(String token) {
        Claims claims = getClaims(token);
        return "initial_access".equals(claims.get("type"));
    }

    // 초기 Refresh Token인지 확인하는 메서드
    public boolean isInitialRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "initial_refresh".equals(claims.get("type"));
    }

    // 최종 Access Token인지 확인하는 메서드
    public boolean isFinalAccessToken(String token) {
        Claims claims = getClaims(token);
        return "final_access".equals(claims.get("type"));
    }

    // 최종 Refresh Token인지 확인하는 메서드
    public boolean isFinalRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "final_refresh".equals(claims.get("type"));
    }

    // 테스트 Access Token인지 확인하는 메서드
    public boolean isTestAccessToken(String token) {
        Claims claims = getClaims(token);
        return "test_access".equals(claims.get("type"));
    }

    // 테스트 Refresh Token인지 확인하는 메서드
    public boolean isTestRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "test_refresh".equals(claims.get("type"));
    }
}
