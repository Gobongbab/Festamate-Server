package com.gobongbob.festamate.domain.sms.application;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
public class TokyoSnsService {

    private final SnsClient snsClient; // AWS SNS 클라이언트
    private final RedisTemplate<String, VerificationInfo> redisTemplate;

    @Autowired
    public TokyoSnsService(SnsClient snsClient,
            @Qualifier("verificationInfoRedisTemplate") // 주입할 빈의 이름을 명시
            RedisTemplate<String, VerificationInfo> redisTemplate) {
        this.snsClient = snsClient;
        this.redisTemplate = redisTemplate;
    }

    // 인증 코드 유효 시간을 Duration으로 정의 (100일)
    private static final Duration CODE_VALID_DURATION = Duration.ofDays(
            100);
    private static final String VERIFICATION_PREFIX = "verification:"; // Redis 키 접두사

    public void sendVerificationCode(String phoneNumber) {
        String formattedPhone = formatToE164(phoneNumber); // <- 포맷 처리

        String verificationCode = generateVerificationCode();
        String redisKey = VERIFICATION_PREFIX + phoneNumber; // Redis 키 생성 (원본 번호 기준)

        // VerificationInfo 생성 (timestamp 대신 Redis TTL 사용)
        VerificationInfo info = new VerificationInfo(
                verificationCode, false);
        redisTemplate.opsForValue().set(redisKey, info,
                CODE_VALID_DURATION); // Redis에 인증 정보 저장 (ValueOperations 사용, 유효 시간 설정)

        // SNS 문자 전송
        PublishRequest request = PublishRequest.builder()
                .message("Festamate! 인증번호는 [" + verificationCode + "] 입니다.")
                .phoneNumber(formattedPhone) // 포맷된 번호 사용
                .build();
        PublishResponse result = snsClient.publish(request);

        System.out.println(
                "📩 Redis 저장 및 인증번호 전송: " + verificationCode + " (Key: " + redisKey + ")");
    }

    public void verifyCode(String phoneNumber, String inputCode) {
        String redisKey = VERIFICATION_PREFIX + phoneNumber;
        // Redis에서 인증 정보 조회
        VerificationInfo info = redisTemplate.opsForValue().get(redisKey);

        if (info == null) {
            // Redis에 키가 없으면 만료되었거나 요청이 없는 경우
            throw new IllegalArgumentException("인증 요청이 존재하지 않거나 만료되었습니다.");
        }
        if (info.isVerified()) { // Getter 사용
            throw new IllegalArgumentException("이미 인증된 번호입니다.");
        }
        // 만료 체크는 Redis TTL이 담당하므로 제거

        // 입력 코드와 저장된 코드 비교 로직
        if (!info.getCode().equals(inputCode)) {
            // 실패 시 failCount 업데이트 로직은 제거하고, 바로 예외 발생
            throw new IllegalArgumentException("인증번호가 틀렸습니다.");
        }

        // 인증 성공 시 로직 (코드가 일치하는 경우)
        // verified 상태를 true로 변경하고 Redis에 업데이트 (기존 TTL 유지)
        info.setVerified(true);
        Long expire = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS); // 남은 만료 시간 조회
        if (expire != null && expire > 0) {
            redisTemplate.opsForValue()
                    .set(redisKey, info, Duration.ofSeconds(expire)); // 남은 시간으로 다시 설정
        } else {
            // 만료 직전이거나 TTL 조회가 안되는 경우 - 안전하게 기본 유효 시간으로 다시 설정
            redisTemplate.opsForValue()
                    .set(redisKey, info, CODE_VALID_DURATION);
        }

        System.out.println("✅ 인증 성공: " + phoneNumber);
    }

    public boolean isPhoneNumberVerified(String phoneNumber) {
        String redisKey = VERIFICATION_PREFIX + phoneNumber;
        VerificationInfo info = redisTemplate.opsForValue().get(redisKey);
        // Redis에 정보가 있고, verified 필드가 true이면 인증된 것
        return info != null && info.isVerified();
    }

    public void removeVerificationInfo(String phoneNumber) {
        String redisKey = VERIFICATION_PREFIX + phoneNumber;
        redisTemplate.delete(redisKey); // Redis에서 키 삭제
        System.out.println("🗑️ Redis 인증 정보 삭제: " + phoneNumber);
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

    @Getter
    @Setter
    @NoArgsConstructor // Jackson 역직렬화를 위해 추가
    @AllArgsConstructor
    public static class VerificationInfo {

        private String code;
        private boolean verified;
    }
}
