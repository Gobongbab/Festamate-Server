package com.gobongbob.festamate.domain.auth.oauth.dto.request;

import lombok.Getter;

@Getter
public class LoginWithKakaoRequest {

    private String kakaoAccessToken;

    public void setKakaoAccessToken(String kakaoAccessToken) {
        this.kakaoAccessToken = kakaoAccessToken;
    }
}
