package com.gobongbob.festamate.domain.auth.jwt.application;

import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshJwtTokenService refreshJwtTokenService;
    private final MemberService memberService;


    // 새로운 초기 Access Token 생성 메서드
    public String createNewInitialAccessToken(String refreshToken) {
        validateInitialRefreshToken(refreshToken);
        Long memberId = getMemberIdFromRefreshToken(refreshToken);
        Member member = memberService.findById(memberId);
        return tokenProvider.generateInitialAccessToken(member);
    }

    // 새로운 최종 Access Token 생성 메서드
    public String createNewFinalAccessToken(String refreshToken) {
        validateFinalRefreshToken(refreshToken);
        Long memberId = getMemberIdFromRefreshToken(refreshToken);
        Member member = memberService.findById(memberId);
        return tokenProvider.generateFinalAccessToken(member);
    }

    // 새로운 테스트용 Access Token 생성 메서드
    public String createNewTestAccessToken(String refreshToken) {
        validateTestRefreshToken(refreshToken);
        Long memberId = getMemberIdFromRefreshToken(refreshToken);
        Member member = memberService.findById(memberId);
        return tokenProvider.generateTestAccessToken(member);
    }

    // 초기 Refresh Token 유효성 및 타입 확인 메서드
    private void validateInitialRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isInitialRefreshToken(
                refreshToken)) {
            throw new IllegalArgumentException("Invalid or unexpected initial refresh token");
        }
    }

    // 최종 Refresh Token 유효성 및 타입 확인 메서드
    private void validateFinalRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isFinalRefreshToken(
                refreshToken)) {
            throw new IllegalArgumentException("Invalid or unexpected final refresh token");
        }
    }

    // 테스트용 Refresh Token 유효성 및 타입 확인 메서드
    private void validateTestRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isTestRefreshToken(
                refreshToken)) {
            throw new IllegalArgumentException("Invalid or unexpected test refresh token");
        }
    }

    // Refresh Token으로 회원 ID 조회 메서드
    private Long getMemberIdFromRefreshToken(String refreshToken) {
        return refreshJwtTokenService.findByRefreshToken(refreshToken).getMemberId();
    }

    // 최종 JWT(access, refresh) 생성 메서드
    public Map<String, String> generateTokens(Long userId) {
        // userId로 Member 객체를 가져오기
        Member member = memberService.findMembersById(userId); // Member 객체로 반환

        // Member 객체를 사용하여 Access Token과 Refresh Token 생성
        String accessToken = tokenProvider.generateFinalAccessToken(member);  // Member 사용
        String refreshToken = tokenProvider.generateFinalRefreshToken(member); // Member 사용

        // JWT 토큰들을 Map으로 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }
}
