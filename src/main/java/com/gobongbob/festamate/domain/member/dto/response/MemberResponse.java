package com.gobongbob.festamate.domain.member.dto.response;

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
        String major
) {

    public static MemberResponse fromEntity(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getStudentId(),
                member.getLoginId(),
                member.getLoginPassword(),
                member.getPhoneNumber(),
                member.getGender().getName(),
                member.getMajor().getDepartment()
        );
    }
}
