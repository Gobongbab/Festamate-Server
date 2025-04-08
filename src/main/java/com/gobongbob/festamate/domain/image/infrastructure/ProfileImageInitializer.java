package com.gobongbob.festamate.domain.image.infrastructure;

import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileImageInitializer {

    private final ProfileImageRepository profileImageRepository;

    @PostConstruct
    public void initDefaultProfileImage() {
        profileImageRepository.findByStoreName("default_profile_image.png")
                .ifPresentOrElse(
                        profileImage -> log.info("✅ 기본 프로필 이미지가 이미 존재합니다."),
                        () -> {
                            saveDefaultProfileImage();
                            log.info("✅ 기본 프로필 이미지를 저장했습니다.");
                        }
                );
    }

    private void saveDefaultProfileImage() {
        ProfileImage defaultProfileImage = ProfileImage.builder()
                .image(Image.builder()
                        .url("https://w7.pngwing.com/pngs/665/132/png-transparent-user-defult-avatar.png")
                        .storeName("default_profile_image.png")
                        .uploadName("default_profile_image.png")
                        .build())
                .build();

        profileImageRepository.save(defaultProfileImage);
    }
}
