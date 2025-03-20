package com.gobongbob.festamate.domain.auth.jwt.persistence;

import com.gobongbob.festamate.domain.auth.jwt.domain.RefreshJwtToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 데이터베이스에서 JWT 리프레시 토큰을 조회하는 기능
public interface RefreshTokenRepository extends JpaRepository<RefreshJwtToken, Long> {

    Optional<RefreshJwtToken> findByMemberId(Long memberId);

    Optional<RefreshJwtToken> findByRefreshToken(String refreshToken);
}
