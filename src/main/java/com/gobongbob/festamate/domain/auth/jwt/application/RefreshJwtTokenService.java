package com.gobongbob.festamate.domain.auth.jwt.application;

import com.gobongbob.festamate.domain.auth.jwt.domain.RefreshJwtToken;
import com.gobongbob.festamate.domain.auth.jwt.persistence.RefreshTokenRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.gobongbob.festamate.global.response.ResponseCode.UNEXPECTED_TOKEN;

// RefreshTokenRepository를 사용하여 데이터베이스에서 리프레시 토큰을 조회하며, 자체 JWT 리프레시 토큰을 관리함
@RequiredArgsConstructor
@Service
public class RefreshJwtTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshJwtToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadRequestException(UNEXPECTED_TOKEN));
    }
}
