package com.gobongbob.festamate.domain.sms.application;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
public class TokyoSnsService {

    private final SnsClient snsClient; // AWS SNS 클라이언트
    private final StringRedisTemplate stringRedisTemplate;

    // 인증 코드 유효 시간 (Duration 사용) - 예: 5분
    private static final Duration CODE_VALID_DURATION = Duration.ofMinutes(5);
    private static final String VERIFICATION_PREFIX = "verification:"; // Redis 키 접두사

    @Autowired // 명시적 생성자 주입
    public TokyoSnsService(SnsClient snsClient,
            StringRedisTemplate stringRedisTemplate) {
        this.snsClient = snsClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void sendVerificationCode(String phoneNumber) {
        String formattedPhone = formatToE164(phoneNumber);
        String verificationCode = generateVerificationCode();
        String redisKey = VERIFICATION_PREFIX + phoneNumber;

        // 인증 코드 문자열만 Redis에 저장
        stringRedisTemplate.opsForValue().set(redisKey, verificationCode, CODE_VALID_DURATION);

        // 문자 발송 로직
        PublishRequest request = PublishRequest.builder()
                .message("Festamate! 인증번호는 [" + verificationCode + "] 입니다.")
                .phoneNumber(formattedPhone)
                .build();
        PublishResponse result = snsClient.publish(request);
    }

    // @Transactional 제거 (Redis 작업은 보통 단일 작업)
    public void verifyCode(String phoneNumber, String inputCode) {
        String redisKey = VERIFICATION_PREFIX + phoneNumber;

        // Redis에서 인증 코드 문자열 조회
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            // HashMap null 체크 대신 Redis 조회 결과 사용
            throw new IllegalArgumentException("인증 요청이 존재하지 않거나 만료되었습니다.");
        }

        if (!storedCode.equals(inputCode)) {
            throw new IllegalArgumentException("인증번호가 틀렸습니다.");
        }

        // 인증 성공 시 로직: Redis에서 키 삭제
        stringRedisTemplate.delete(redisKey);

    }

    public void removeVerificationInfo(String phoneNumber) {
        String redisKey = VERIFICATION_PREFIX + phoneNumber;
        // HashMap 제거 로직 대신 Redis 삭제 사용
        Boolean deleted = stringRedisTemplate.delete(redisKey);
    }

    private String generateVerificationCode() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6자리 랜덤
    }

    /**
     * 전화번호를 +82로 시작하는 E.164 국제 표준 포맷으로 변환
     */
    private String formatToE164(String rawPhoneNumber) {
        // 하이픈 제거
        String digitsOnly = rawPhoneNumber.replaceAll("-", "");
        // 01012345678 형태면 +82로 변환
        if (digitsOnly.startsWith("0")) {
            return "+82" + digitsOnly.substring(1);
        }
        // 이미 +로 시작하면 그대로
        return digitsOnly;
    }
}
