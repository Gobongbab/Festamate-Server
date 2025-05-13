package com.gobongbob.festamate.domain.coupon.domain;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_USED_TICKET;
import static com.gobongbob.festamate.global.response.ResponseCode.EXPIRED_TICKET;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.entity.BaseEntity;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE coupon SET deleted = true, deleted_at = now() WHERE id = ?")
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
            throw new BadRequestException(ALREADY_USED_TICKET);
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            throw new BadRequestException(EXPIRED_TICKET);
        }
        this.used = true;
    }
}
