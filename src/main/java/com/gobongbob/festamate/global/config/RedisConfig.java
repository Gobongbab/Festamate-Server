//package com.gobongbob.festamate.global.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
//import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.gobongbob.festamate.domain.sms.application.TokyoSnsService.VerificationInfo;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public ObjectMapper objectMapper() {
//        // PolymorphicTypeValidator 설정: 모든 서브타입 허용 (보안상 필요한 경우 더 제한적으로 설정 가능)
//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
//                .builder()
//                .allowIfSubType(Object.class) // 모든 클래스 허용 (가장 일반적)
//                // .allowIfBaseType(VerificationInfo.class) // 특정 기본 타입만 허용
//                // .allowIfSubTypeIsArray() // 배열 타입 허용
//                .build();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈 등록
//        // 타입 정보 포함 설정: NON_FINAL 타입에 대해 @class 속성 추가
//        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
//        // 필요에 따라 다른 ObjectMapper 설정 추가 가능
//        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return objectMapper;
//    }
//
//
//    @Bean
//    public RedisTemplate<String, VerificationInfo> verificationInfoRedisTemplate(
//            RedisConnectionFactory connectionFactory,
//            // 설정된 ObjectMapper 빈을 주입받음
//            ObjectMapper objectMapper) {
//
//        // 주입받은 objectMapper 사용
//        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(
//                objectMapper);
//
//        RedisTemplate<String, VerificationInfo> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(connectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());       // Key: String 형식
//        redisTemplate.setValueSerializer(
//                valueSerializer);              // Value: VerificationInfo -> JSON 형식
//        redisTemplate.setHashKeySerializer(
//                new StringRedisSerializer());    // Hash Key: String 형식 (필요시)
//        redisTemplate.setHashValueSerializer(valueSerializer);         // Hash Value: JSON 형식 (필요시)
//        redisTemplate.afterPropertiesSet(); // 설정 적용
//        return redisTemplate;
//    }
//}
