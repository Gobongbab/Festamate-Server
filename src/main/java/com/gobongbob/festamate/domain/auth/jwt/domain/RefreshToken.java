package com.gobongbob.festamate.domain.auth.jwt.domain;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

// Redis에 저장될 리프레시 토큰을 저장하는 클래스
// @RedisHash 어노테이션을 통해 Redis에 저장될 때의 key prefix와 TTL을 설정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken", timeToLive = 1209600)
// Redis 설정: key는 "refreshToken:[id값]" 형태, TTL은 14일 (14 * 24 * 60 * 60 = 1209600초)
public class RefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id; // 사용자 ID

    @Indexed
    private String refreshTokenValue; // 리프레시 토큰 값
}
