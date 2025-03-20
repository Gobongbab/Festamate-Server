package com.gobongbob.festamate.domain.auth.jwt.application;

import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshJwtTokenService refreshJwtTokenService;
    private final MemberService memberService;

    public String createNewAccessToken(String refreshToken) {
        // Refresh Token 유효성 및 타입 확인
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(
                refreshToken)) {
            throw new IllegalArgumentException("Invalid or unexpected token");
        }

        // Refresh Token으로 회원 ID 조회
        Long memberId = refreshJwtTokenService.findByRefreshToken(refreshToken).getMemberId();
        Member member = memberService.findById(memberId);

        // 새로운 Access Token 생성
        return tokenProvider.generateAccessToken(member);
    }
}
