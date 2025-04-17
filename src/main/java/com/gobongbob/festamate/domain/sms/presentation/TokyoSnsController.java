package com.gobongbob.festamate.domain.sms.presentation;

import com.gobongbob.festamate.domain.sms.application.TokyoSnsService;
import com.gobongbob.festamate.domain.sms.dto.request.PhoneRequest;
import com.gobongbob.festamate.domain.sms.dto.request.PhoneVerifyRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
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
    public SuccessResponse<String> sendVerificationCode(
            @RequestBody PhoneRequest request) {
        tokyoSnsService.sendVerificationCode(request.phoneNumber());
        return new SuccessResponse<>("인증번호 전송 성공");
    }

    // 2. 인증번호 확인
    @PostMapping("/verify")
    public SuccessResponse<String> verifyCode(
            @RequestBody PhoneVerifyRequest request) {
        tokyoSnsService.verifyCode(request.phoneNumber(), request.code());
        return new SuccessResponse<>("인증 성공");
    }
}
