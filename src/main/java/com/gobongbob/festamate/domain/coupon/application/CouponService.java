package com.gobongbob.festamate.domain.coupon.application;

import com.gobongbob.festamate.domain.coupon.domain.Coupon;
import com.gobongbob.festamate.domain.coupon.dto.request.UseCouponRequest;
import com.gobongbob.festamate.domain.coupon.persistence.CouponRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
        coupon.assignToMember(member);
        member.increaseMaximumTicket();
        member.setRemainingTicket(member.getMaximumTicket());
    }

    @Transactional
    public void initializeCoupons(Member member) {
        // 요청자가 관리자인지 확인하는 로직 필요

        Set<Coupon> coupons = new HashSet<>();
        while (coupons.size() < COUPON_COUNT) {
            String code = generateRandomCode();
            coupons.add(
                    Coupon.builder()
                            .code(code)
                            .expiresAt(LocalDateTime.now().plusDays(100))
                            .build()
            );
        }
        couponRepository.saveAll(coupons);
    }

    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(COUPON_LENGTH);
        for (int i = 0; i < COUPON_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        System.out.println("Coupon Code = " + sb);

        return sb.toString();
    }
}
