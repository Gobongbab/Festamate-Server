package com.gobongbob.festamate.domain.coupon.application;

import com.gobongbob.festamate.domain.coupon.domain.Coupon;
import com.gobongbob.festamate.domain.coupon.dto.request.UseCouponRequest;
import com.gobongbob.festamate.domain.coupon.persistence.CouponRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private static final int COUPON_COUNT = 100;
    private static final int COUPON_LENGTH = 6;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Transactional
    public void useCoupon(Member member, UseCouponRequest request) {
        Coupon coupon = couponRepository.findByCode(request.code())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        coupon.useCoupon();
        member.setMaximumTicket(3);
    }
}
