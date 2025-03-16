package com.gobongbob.festamate.domain.auth.jwt.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.CreateAccessTokenRequest;
import com.gobongbob.festamate.domain.auth.jwt.dto.response.CreateAccessTokenResponse;
import com.gobongbob.festamate.global.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 자체 JWT 토큰을 생성하고 반환함
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class TokenController {

    private final TokenService tokenService;
    private final TokenProvider tokenProvider;

    @PostMapping("/refresh") // refresh jwt로 access jwt 받아옴
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @RequestBody CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }
}
