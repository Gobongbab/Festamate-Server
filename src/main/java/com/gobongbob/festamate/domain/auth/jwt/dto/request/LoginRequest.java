package com.gobongbob.festamate.domain.auth.jwt.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    private String loginId;
    private String password; // 클라이언트가 보내는 암호화되지 않은 원본 비밀번호
}
