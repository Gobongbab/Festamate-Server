package com.gobongbob.festamate.domain.coupon.persistence;

import com.gobongbob.festamate.domain.coupon.domain.Coupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("select c from Coupon c where c.code = ?1")
    Optional<Coupon> findByCode(String code);
}
