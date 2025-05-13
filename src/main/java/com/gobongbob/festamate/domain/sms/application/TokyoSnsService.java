package com.gobongbob.festamate.domain.sms.application;

import static com.gobongbob.festamate.global.response.ResponseCode.AUTH_CODE_MISMATCH;
import static com.gobongbob.festamate.global.response.ResponseCode.AUTH_CODE_NOT_FOUND_OR_EXPIRED;
import static com.gobongbob.festamate.global.response.ResponseCode.AUTH_REQUEST_DAILY_LIMIT_EXCEEDED;

import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
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

    // 인증 코드 유효 시간 (Duration 사용): 5분
    private static final Duration CODE_VALID_DURATION = Duration.ofMinutes(5);
    private static final String VERIFICATION_PREFIX = "verification:"; // Redis 키 접두사

    // 재전송 횟수 제한 관련 설정
    private static final String RETRY_COUNT_PREFIX = "retry_count:"; // 재전송 횟수 키 접두사
    private static final int MAX_RETRY_COUNT = 2; // 하루 최대 재전송 횟수: 2번
    private static final Duration RETRY_COUNT_VALID_DURATION = Duration.ofDays(1); // 재전송 횟수 카운트 유효 기간: 24시간

    @Autowired
    public TokyoSnsService(SnsClient snsClient,
            StringRedisTemplate stringRedisTemplate) {
        this.snsClient = snsClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void sendVerificationCode(String phoneNumber) {
        String formattedPhone = formatToE164(phoneNumber); // 문자 발송용
        String normalizedPhone = normalizePhoneNumber(phoneNumber); // Redis Key용
        String redisKey = VERIFICATION_PREFIX + normalizedPhone;
        String retryCountKey = RETRY_COUNT_PREFIX + normalizedPhone;

        // 1. 재전송 횟수 확인
        String currentRetryCountStr = stringRedisTemplate.opsForValue().get(retryCountKey);
        int currentRetryCount = 0;
        if (currentRetryCountStr != null) {
            currentRetryCount = Integer.parseInt(currentRetryCountStr);
        }

        if (currentRetryCount >= MAX_RETRY_COUNT) {
            Long ttl = stringRedisTemplate.getExpire(retryCountKey, TimeUnit.SECONDS);
            String timeLeftMessage = "";
            if (ttl != null && ttl > 0) {
                long hours = ttl / 3600;
                long minutes = (ttl % 3600) / 60;
                long seconds = ttl % 60;
                timeLeftMessage = String.format(" (다음 요청 가능 시간: 약 %d시간 %d분 %d초 후)", hours, minutes, seconds);
            }
            throw new BadRequestException(AUTH_REQUEST_DAILY_LIMIT_EXCEEDED + timeLeftMessage);
        }

        // 2. 기존 인증 코드 삭제 (재전송 시 이전 코드 무효화)
        stringRedisTemplate.delete(redisKey);

        // 3. 새로운 인증 코드 생성 및 저장
        String verificationCode = generateVerificationCode();
        stringRedisTemplate.opsForValue().set(redisKey, verificationCode, CODE_VALID_DURATION);

        // 4. 재전송 횟수 증가 및 유효기간 설정
        Long newRetryCount = stringRedisTemplate.opsForValue().increment(retryCountKey);
        // 처음 횟수가 기록될 때만 유효기간 설정 (이미 키가 존재하고 TTL이 설정되어 있으면 갱신하지 않음)
        if (newRetryCount != null && newRetryCount == 1) {
            stringRedisTemplate.expire(retryCountKey, RETRY_COUNT_VALID_DURATION);
        }

        // 5. 문자 발송 로직
        PublishRequest request = PublishRequest.builder()
                .message("Festamate! 인증번호는 [" + verificationCode + "] 입니다.")
                .phoneNumber(formattedPhone)
                .build();
        PublishResponse result = snsClient.publish(request);
    }

    // @Transactional 제거 (Redis 작업은 보통 단일 작업)
    public void verifyCode(String phoneNumber, String inputCode) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String redisKey = VERIFICATION_PREFIX + normalizedPhone;
        String retryCountKey = RETRY_COUNT_PREFIX + normalizedPhone;

        // Redis에서 인증 코드 문자열 조회
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            // HashMap null 체크 대신 Redis 조회 결과 사용
            throw new BadRequestException(AUTH_CODE_NOT_FOUND_OR_EXPIRED);
        }

        if (!storedCode.equals(inputCode)) {
            throw new BadRequestException(AUTH_CODE_MISMATCH);
        }

        // 인증 성공 시 로직: Redis에서 키 삭제
        stringRedisTemplate.delete(redisKey);
        stringRedisTemplate.delete(retryCountKey); // 인증 성공 시 재시도 횟수 카운트도 초기화

    }

    public void removeVerificationInfo(String phoneNumber) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String redisKey = VERIFICATION_PREFIX + normalizedPhone;
        String retryCountKey = RETRY_COUNT_PREFIX + normalizedPhone;
        stringRedisTemplate.delete(redisKey);
        stringRedisTemplate.delete(retryCountKey); // 정보 삭제 시 재시도 횟수 카운트도 함께 삭제
    }

    // 하이픈 제거
    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("-", "");
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
