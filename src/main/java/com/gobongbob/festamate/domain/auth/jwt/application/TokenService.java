package com.gobongbob.festamate.domain.auth.jwt.application;

import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService { // generateToken()

    private final TokenProvider tokenProvider;
    private final RefreshJwtTokenService refreshJwtTokenService;
    private final MemberService memberService;

    // 토큰 생성 메서드 (사용자별로 토큰을 생성)
    public Map<String, String> generateTokens(Long memberId, TokenType type) {
        Member member = memberService.findById(memberId);
        return tokenProvider.generateTokens(member, type);
    }

    // Refresh Token을 사용하여 새로운 Access Token 생성
    public String createNewAccessToken(String refreshToken, TokenType type) {
        validateRefreshToken(refreshToken);
        Long memberId = tokenProvider.getMemberIdFromRefreshToken(refreshToken);
        Member member = memberService.findById(memberId);
        return tokenProvider.createAccessToken(member, type);
    }

    // Refresh Token 유효성 검사
    private void validateRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
    }
}
