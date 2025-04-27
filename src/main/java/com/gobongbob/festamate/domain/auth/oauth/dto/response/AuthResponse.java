package com.gobongbob.festamate.domain.auth.oauth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthResponse {

    private final String accessToken;
}
