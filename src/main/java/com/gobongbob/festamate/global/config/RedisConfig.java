package com.gobongbob.festamate.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gobongbob.festamate.domain.sms.application.TokyoSnsService.VerificationInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, VerificationInfo> verificationInfoRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        // JSON 직렬화/역직렬화 설정
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(
                objectMapper);

        RedisTemplate<String, VerificationInfo> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());       // Key: String 형식
        redisTemplate.setValueSerializer(
                valueSerializer);              // Value: VerificationInfo -> JSON 형식
        redisTemplate.setHashKeySerializer(
                new StringRedisSerializer());    // Hash Key: String 형식 (필요시)
        redisTemplate.setHashValueSerializer(valueSerializer);         // Hash Value: JSON 형식 (필요시)
        redisTemplate.afterPropertiesSet(); // 설정 적용
        return redisTemplate;
    }
}
