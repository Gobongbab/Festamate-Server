package com.gobongbob.festamate.domain.member.dto.response;

import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;

// 관리자용 유저 조회
public record MemberResponse(
        Long id,
        String name,
        String nickname,
        String studentId,
        String loginId,
        String loginPassword,
        String phoneNumber,
        Gender gender,
        String department,
        String status,
        ImageResponse profileImage
) {

    public static MemberResponse fromEntity(Member member) {
        Image profileImage = member.getProfileImage().getImage();

        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getStudentId().substring(2, 4),
                member.getLoginId(),
                member.getLoginPassword(),
                member.getPhoneNumber(),
                member.getGender(),
                member.getStudentDepartment(),
                member.getStatus().name(),
                ImageResponse.fromEntity(profileImage)
        );
    }
}
