package com.gobongbob.festamate.domain.image.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Transient
    private static final ProfileImage DEFAULT_PROFILE_IMAGE = ProfileImage.builder()
            .image(Image.builder()
                    .url("https://w7.pngwing.com/pngs/665/132/png-transparent-user-defult-avatar.png")
                    .storeName("default_profile_image.png")
                    .uploadName("default_profile_image.png")
                    .build())
            .build();

    public static ProfileImage getDefaultProfileImage() {
        return DEFAULT_PROFILE_IMAGE;
    }
}
