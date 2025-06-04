package com.gobongbob.festamate.domain.ban.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE ban SET deleted = true, deleted_at = now() WHERE id = ?")
public class Ban extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
