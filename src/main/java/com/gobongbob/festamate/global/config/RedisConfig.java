package com.gobongbob.festamate.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean(name = "stringRedisTemplateCustom") // 빈 이름 지정
    public RedisTemplate<String, String> stringRedisTemplateCustom(
            RedisConnectionFactory connectionFactory) {

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(stringSerializer); // Key: String
        redisTemplate.setValueSerializer(stringSerializer); // Value: String
        redisTemplate.setHashKeySerializer(stringSerializer); // Hash Key: String
        redisTemplate.setHashValueSerializer(stringSerializer); // Hash Value: String
        redisTemplate.afterPropertiesSet(); // 설정 적용
        return redisTemplate;
    }
}
