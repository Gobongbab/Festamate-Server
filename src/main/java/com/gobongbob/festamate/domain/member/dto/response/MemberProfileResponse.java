package com.gobongbob.festamate.domain.member.dto.response;

import com.gobongbob.festamate.domain.member.domain.Member;

public record MemberProfileResponse(
        String name,
        String nickname,
        String studentId,
        String phoneNumber,
        String gender,
        String major
) {

    public static MemberProfileResponse fromEntity(Member member) {
        return new MemberProfileResponse(
                member.getName(),
                member.getNickname(),
                member.getStudentId(),
                member.getPhoneNumber(),
                member.getGender().getName(),
                member.getMajor().getDepartment()
        );
    }
}
