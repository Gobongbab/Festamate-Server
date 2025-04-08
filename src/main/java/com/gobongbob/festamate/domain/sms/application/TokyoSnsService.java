package com.gobongbob.festamate.domain.sms.application;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokyoSnsService {

    private final Map<String, VerificationInfo> verificationData = new HashMap<>();
    private static final long CODE_VALID_MILLIS = 3 * 60 * 1000; // 3분
    private static final int MAX_FAIL_COUNT = 5;

    public void sendVerificationCode(String phoneNumber) {
        String verificationCode = generateVerificationCode();

        VerificationInfo info = new VerificationInfo(
                verificationCode, System.currentTimeMillis(), 0, false
        );
        verificationData.put(phoneNumber, info);
        System.out.println("📩 인증번호 전송: " + verificationCode);
    }

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

    @Getter
    @AllArgsConstructor
    static class VerificationInfo {

        private String code;
        private long timestamp;
        private int failCount;
        private boolean verified;
    }
}
