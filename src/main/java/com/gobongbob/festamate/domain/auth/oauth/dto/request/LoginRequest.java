package com.gobongbob.festamate.domain.auth.oauth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String code; // 인가 코드를 저장하는 필드
}
