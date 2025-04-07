package com.gobongbob.festamate.domain.auth.jwt.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.CreateAccessTokenRequest;
import com.gobongbob.festamate.domain.auth.jwt.dto.response.CreateAccessTokenResponse;
import com.gobongbob.festamate.global.util.TokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 자체 JWT 토큰을 생성하고 반환함
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/auth")
public class TokenController {

    private final TokenService tokenService;
    private final TokenProvider tokenProvider;

    @PostMapping("/refresh") // 초기 refresh jwt로 access jwt 받아옴
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @RequestBody @Valid CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewInitialAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

    @PostMapping("/refresh/final") // 최종 refresh jwt로 access jwt 받아옴
    public ResponseEntity<CreateAccessTokenResponse> createNewFinalAccessToken(
            @RequestBody @Valid CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewFinalAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

    @PostMapping("/refresh/test") // 테스트용 refresh jwt로 access jwt 받아옴
    public ResponseEntity<CreateAccessTokenResponse> createNewTestAccessToken(
            @RequestBody @Valid CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewTestAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }
}
