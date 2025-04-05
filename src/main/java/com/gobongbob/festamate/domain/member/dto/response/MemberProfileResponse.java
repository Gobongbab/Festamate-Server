package com.gobongbob.festamate.domain.member.dto.response;

import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Member;

public record MemberProfileResponse(
        String name,
        String nickname,
        String studentId,
        String phoneNumber,
        String gender,
        String major,
        int maximumTicket,
        int remainingTicket,
        ImageResponse profileImage
) {

    public static MemberProfileResponse fromEntity(Member member) {
        Image profileImage = member.getProfileImage().getImage();

        return new MemberProfileResponse(
                member.getName(),
                member.getNickname(),
                member.getStudentId(),
                member.getPhoneNumber(),
                member.getGender() != null ? member.getGender().getName() : "Unknown",
                member.getMajor() != null ? member.getMajor().getDepartment() : "Unknown",
                member.getMaximumTicket(),
                member.getRemainingTicket(),
                ImageResponse.fromEntity(profileImage)
        );
    }
}
