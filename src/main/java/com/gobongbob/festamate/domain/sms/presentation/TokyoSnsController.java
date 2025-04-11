package com.gobongbob.festamate.domain.sms.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.sms.application.TokyoSnsService;
import com.gobongbob.festamate.domain.sms.dto.request.PhoneRequest;
import com.gobongbob.festamate.domain.sms.dto.request.PhoneVerifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/phone")
@RequiredArgsConstructor
public class TokyoSnsController {

    private final TokyoSnsService tokyoSnsService;

    // 1. 인증번호 요청 API
    @PostMapping("/send")
    public ResponseEntity<String> sendVerificationCode(
            @RequestBody PhoneRequest request,
            @AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
        tokyoSnsService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    // 2. 인증번호 확인
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(
            @RequestBody PhoneVerifyRequest request,
            @AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
        tokyoSnsService.verifyCode(request.phoneNumber(), request.code(), customMemberDetails);
        return ResponseEntity.ok("인증 성공");
    }
}
