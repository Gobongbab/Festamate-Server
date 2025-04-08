package com.gobongbob.festamate.domain.image.persistence;

import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    @Query("select p from ProfileImage p where p.image.storeName = ?1")
    Optional<ProfileImage> findByStoreName(String storeName);
}
