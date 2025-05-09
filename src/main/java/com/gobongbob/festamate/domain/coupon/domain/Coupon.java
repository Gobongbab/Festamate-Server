package com.gobongbob.festamate.domain.coupon.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Builder.Default
    private boolean used = false;

    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void assignToMember(Member member) {
        this.member = member;
    }

    public void useCoupon() {
        if (this.used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        this.used = true;
    }
}
