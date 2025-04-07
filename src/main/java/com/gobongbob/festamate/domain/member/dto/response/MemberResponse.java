package com.gobongbob.festamate.domain.member.dto.response;

import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Member;

public record MemberResponse(
        Long id,
        String name,
        String nickname,
        String studentId,
        String loginId,
        String loginPassword,
        String phoneNumber,
        String gender,
        String major,
        ImageResponse profileImage
) {

    public static MemberResponse fromEntity(Member member) {
        ProfileImage profileImage = member.getProfileImage();
        if (profileImage == null) {
            profileImage = ProfileImage.getDefaultProfileImage();
        }

        Image image = profileImage.getImage();

        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getStudentId(),
                member.getLoginId(),
                member.getLoginPassword(),
                member.getPhoneNumber(),
                member.getGender().getName(),
                member.getMajor().getDepartment(),
                ImageResponse.fromEntity(image)
        );
    }
}
