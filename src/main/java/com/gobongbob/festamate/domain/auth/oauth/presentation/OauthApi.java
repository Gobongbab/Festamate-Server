package com.gobongbob.festamate.domain.auth.oauth.presentation;

import com.gobongbob.festamate.domain.auth.oauth.dto.request.KakaoLoginRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.LoginWithKakaoRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoCheckResponse;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Oauth", description = "Oauth 관련 API")
public interface OauthApi {

    @Operation(summary = "카카오 회원 여부 확인", description = "카카오 인가 코드를 사용하여 회원 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    SuccessResponse<KakaoCheckResponse> kakaoCheck(
            @Parameter(description = "카카오 로그인 요청 정보")
            @RequestBody @Valid KakaoLoginRequest kakaoLoginRequest
    );

    @Operation(summary = "기존 회원 로그인", description = "카카오 Access Token을 사용하여 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    ResponseEntity<SuccessResponse<AuthResponse>> loginWithKakao(
            @Parameter(description = "카카오 로그인 요청 정보")
            @RequestBody LoginWithKakaoRequest request,
            HttpServletResponse response
    );

    @Operation(summary = "신규 회원 프로필 등록", description = "신규 회원의 프로필을 등록하고 JWT를 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    ResponseEntity<SuccessResponse<AuthResponse>> registerProfile(
            @Parameter(description = "프로필 등록 요청 정보")
            @RequestBody ProfileRegisterRequest request,
            HttpServletResponse response
    );
}