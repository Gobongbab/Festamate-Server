package com.gobongbob.festamate.domain.auth.jwt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateAccessTokenResponse {

    private String accessToken; // jwt access
}
