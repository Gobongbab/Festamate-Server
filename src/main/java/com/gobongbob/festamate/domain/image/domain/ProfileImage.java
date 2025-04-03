package com.gobongbob.festamate.domain.image.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Image image;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Transient
    private static final ProfileImage DEFAULT_PROFILE_IMAGE = ProfileImage.builder()
            .image(Image.builder()
                    .url("https://w7.pngwing.com/pngs/665/132/png-transparent-user-defult-avatar.png")
                    .storeName("default_profile_image.png")
                    .uploadName("default_profile_image.png")
                    .build())
            .member(null) // 기본 이미지라 특정 유저와 연결되지 않음
            .build();

    public static ProfileImage getDefaultProfileImage() {
        return DEFAULT_PROFILE_IMAGE;
    }

    // 연관관계 편의 메서드
    public void setMember(Member member) {
        this.member = member;
    }
}
