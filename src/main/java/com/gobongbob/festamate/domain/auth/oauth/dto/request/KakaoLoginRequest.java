package com.gobongbob.festamate.domain.auth.oauth.dto.request;

import lombok.Getter;

@Getter
public class KakaoLoginRequest {

    private String code; // 인가 코드
}
