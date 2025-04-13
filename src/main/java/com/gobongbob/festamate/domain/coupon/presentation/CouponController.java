package com.gobongbob.festamate.domain.coupon.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.coupon.application.CouponService;
import com.gobongbob.festamate.domain.coupon.dto.request.UseCouponRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "쿠폰 사용", description = "쿠폰을 사용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 쿠폰입니다.")
    })
    @PostMapping("")
    public SuccessResponse<Void> useCoupon(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "쿠폰 사용 요청 정보")
            @RequestBody UseCouponRequest request
    ) {
        couponService.useCoupon(memberDetails.getMember(), request);

        return new SuccessResponse<>();
    }

    @Operation(summary = "쿠폰 초기화", description = "사용자의 쿠폰을 초기화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PostMapping("/init")
    public SuccessResponse<Void> initializeCoupons(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        couponService.initializeCoupons(memberDetails.getMember());

        return new SuccessResponse<>();
    }
}