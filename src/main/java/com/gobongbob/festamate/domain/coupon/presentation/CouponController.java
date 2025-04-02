package com.gobongbob.festamate.domain.coupon.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.coupon.application.CouponService;
import com.gobongbob.festamate.domain.coupon.dto.request.UseCouponRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("")
    public ResponseEntity<Void> useCoupon(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody UseCouponRequest request
    ) {
        couponService.useCoupon(memberDetails.getMember(), request);

        return ResponseEntity.ok().build();
    }
}
