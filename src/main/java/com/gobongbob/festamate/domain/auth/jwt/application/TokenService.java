package com.gobongbob.festamate.domain.auth.jwt.application;

import com.gobongbob.festamate.domain.auth.jwt.domain.RefreshToken;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.jwt.persistence.RefreshTokenRepository;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// JWT 토큰 생성, 재발급 및 Redis 연동 관리를 담당하는 서비스 클래스
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final RefreshTokenRepository refreshRepository;
    private final MemberRepository memberRepository;

    // 로그인 시 최초 토큰 생성 및 리프레시 토큰 Redis 저장
    @Transactional // DB 조회(Member) 및 Redis 저장(Token)
    public Map<String, String> generateAndSaveTokens(Long memberId, TokenType accessType) {
        Member member = memberService.findById(memberId); // DB에서 사용자 조회
        Map<String, String> tokens = tokenProvider.generateTokens(member, accessType); // 토큰 생성

        // 리프레시 토큰 Redis에 저장 (사용자 ID는 String으로 변환하여 저장)
        saveRefreshTokenToRedis(String.valueOf(memberId), tokens.get("refreshToken"));

        return tokens; // 생성된 토큰들 반환
    }

    // 액세스 토큰 재발급 (Refresh Token Rotation 적용)
    @Transactional // Redis 조회/삭제/저장 및 DB 조회
    public TokenRefreshResult regenerateAccessToken(String refreshTokenValueFromCookie) {
        // 1. Redis에서 리프레시 토큰 조회 (값으로 조회)
        RefreshToken foundRefreshToken = refreshRepository.findByRefreshTokenValue(
                        refreshTokenValueFromCookie)
                .orElseThrow(() -> new IllegalArgumentException(
                        "유효하지 않거나 만료된 리프레시 토큰입니다.")); // Redis에 없으면 예외 발생

        // 2. 사용자 ID 추출 (Redis Key 값 사용) 및 Member 객체 조회
        String userIdString = foundRefreshToken.getId(); // Redis Key (String 타입 사용자 ID)
        Long memberId = parseLongUserId(userIdString); // DB 조회를 위해 Long으로 변환
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "토큰에 해당하는 사용자를 찾을 수 없습니다. ID: " + memberId)); // DB에 사용자 없으면 예외 발생

        // 3. 기존 리프레시 토큰 Redis에서 삭제
        refreshRepository.delete(foundRefreshToken);

        // 4. 새로운 액세스 토큰 및 리프레시 토큰 생성
        Map<String, String> newTokens = tokenProvider.generateTokens(member,
                TokenType.FINAL_ACCESS);
        String newAccessToken = newTokens.get("accessToken");
        String newRefreshTokenValue = newTokens.get("refreshToken");

        // 5. 새로운 리프레시 토큰 Redis에 저장 (새 TTL 적용됨)
        saveRefreshTokenToRedis(userIdString, newRefreshTokenValue); // String 타입 ID 사용

        // 6. 결과 반환 (새 액세스 토큰 + 새 리프레시 토큰)
        return new TokenRefreshResult(newAccessToken, newRefreshTokenValue);
    }

    // Redis에 리프레시 토큰 저장 (로그인 및 재발급 시 사용)
    private void saveRefreshTokenToRedis(String userId, String refreshTokenValue) {
        RefreshToken token = RefreshToken.builder()
                .id(userId) // String 타입 ID
                .refreshTokenValue(refreshTokenValue)
                .build();
        refreshRepository.save(token); // Redis에 저장 (TTL 자동 적용)
    }

    // 로그아웃 시 Redis에서 리프레시 토큰 삭제
    @Transactional
    public void deleteRefreshTokenFromRedis(String userId) {
        // CrudRepository<RefreshToken, String> 이므로 String ID 전달
        refreshRepository.deleteById(userId); // Redis에서 해당 ID의 데이터 삭제
    }

    // Redis Key(String)에서 가져온 사용자 ID를 Long 타입으로 변환하는 헬퍼 메서드
    private Long parseLongUserId(String userIdString) {
        try {
            return Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Redis 키의 사용자 ID 형식이 올바르지 않습니다: " + userIdString);
        }
    }

    // 토큰 재발급 결과를 담는 내부 클래스 (또는 별도 DTO 파일로 분리 가능)
    @Getter
    @RequiredArgsConstructor
    public static class TokenRefreshResult {

        private final String accessToken; // 새로 발급된 액세스 토큰
        private final String refreshToken; // 새로 발급된 리프레시 토큰 (쿠키 설정을 위해 Controller에 전달)
    }
}
