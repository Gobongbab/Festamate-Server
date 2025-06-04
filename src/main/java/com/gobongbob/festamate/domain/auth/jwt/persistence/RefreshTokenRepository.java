package com.gobongbob.festamate.domain.auth.jwt.persistence;

import com.gobongbob.festamate.domain.auth.jwt.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

// Redis에서 JWT 리프레시 토큰을 조회하는 기능
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByRefreshTokenValue(String refreshTokenValue);

}
