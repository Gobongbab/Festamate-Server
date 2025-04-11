package com.gobongbob.festamate.domain.auth.oauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoCheckResponse {

    private boolean isMember;
    private String kakaoAccessToken;
}
