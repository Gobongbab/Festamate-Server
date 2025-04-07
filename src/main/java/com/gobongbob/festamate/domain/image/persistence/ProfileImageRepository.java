package com.gobongbob.festamate.domain.image.persistence;

import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    Optional<ProfileImage> findByStoreName(String storeName);
}
