package com.gobongbob.festamate.domain.sms.presentation;

import com.gobongbob.festamate.domain.sms.dto.request.PhoneRequest;
import com.gobongbob.festamate.domain.sms.dto.request.PhoneVerifyRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "TokyoSns", description = "Tokyo SMS 관련 API")
public interface TokyoSnsApi {

    @Operation(summary = "인증번호 요청", description = "전화번호로 인증번호를 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    SuccessResponse<String> sendVerificationCode(
            @Parameter(description = "전화번호 요청 정보")
            @RequestBody PhoneRequest request
    );

    @Operation(summary = "인증번호 확인", description = "전화번호와 인증번호를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    SuccessResponse<String> verifyCode(
            @Parameter(description = "인증번호 확인 요청 정보")
            @RequestBody PhoneVerifyRequest request
    );
}