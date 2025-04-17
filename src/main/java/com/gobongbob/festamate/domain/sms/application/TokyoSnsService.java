package com.gobongbob.festamate.domain.sms.application;

import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
@RequiredArgsConstructor
public class TokyoSnsService {

    private final SnsClient snsClient; // AWS SNS 클라이언트
    private final Map<String, VerificationInfo> verificationData = new HashMap<>();
    private static final long CODE_VALID_MILLIS = 5 * 60 * 1000; // 5분
    private static final int MAX_FAIL_COUNT = 5;
    private final MemberRepository memberRepository;

    public void sendVerificationCode(String phoneNumber) {
        String formattedPhone = formatToE164(phoneNumber); // <- 포맷 처리

        String verificationCode = generateVerificationCode();

        VerificationInfo info = new VerificationInfo(
                verificationCode, System.currentTimeMillis(), 0, false
        );
        verificationData.put(phoneNumber, info); // 원래 입력된 번호로 저장

        // SNS 문자 전송
        PublishRequest request = PublishRequest.builder()
                .message("Festamate! 인증번호는 [" + verificationCode + "] 입니다.")
                .phoneNumber(formattedPhone) // 포맷된 번호 사용
                .build();
        PublishResponse result = snsClient.publish(request);

        System.out.println("📩 인증번호 전송: " + verificationCode);
    }

    @Transactional
    public void verifyCode(String phoneNumber, String inputCode) {
        VerificationInfo info = verificationData.get(phoneNumber);
        if (info == null) {
            throw new IllegalArgumentException("인증 요청이 존재하지 않습니다.");
        }
        if (info.verified) {
            throw new IllegalArgumentException("이미 인증된 번호입니다.");
        }
        if (System.currentTimeMillis() - info.timestamp > CODE_VALID_MILLIS) {
            verificationData.remove(phoneNumber);
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }
        if (info.failCount >= MAX_FAIL_COUNT) {
            throw new IllegalArgumentException("실패 횟수 초과. 다시 요청해주세요.");
        }
        if (!info.code.equals(inputCode)) {
            info.failCount += 1;
            throw new IllegalArgumentException("인증번호가 틀렸습니다.");
        }

        info.verified = true; // 인증 완료
    }

    public boolean isPhoneNumberVerified(String phoneNumber) {
        VerificationInfo info = verificationData.get(phoneNumber);
        return info != null && info.verified;
    }

    public void removeVerificationInfo(String phoneNumber) {
        verificationData.remove(phoneNumber); // 등록 후 제거
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
    @AllArgsConstructor
    static class VerificationInfo {

        private String code;
        private long timestamp;
        private int failCount;
        private boolean verified;
    }
}
