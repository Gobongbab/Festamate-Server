package com.gobongbob.festamate.domain.auth.jwt.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccessTokenRequest {

    private String refreshToken;
}
