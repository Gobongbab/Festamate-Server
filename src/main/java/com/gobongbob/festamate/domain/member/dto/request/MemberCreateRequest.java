package com.gobongbob.festamate.domain.member.dto.request;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;

public record MemberCreateRequest(
        String name,
        String nickname,
        String studentId,
        String loginId,
        String loginPassword,
        String phoneNumber,
        String gender,
        String college,
        String department
) {

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .nickname(nickname)
                .studentId(studentId)
                .loginId(loginId)
                .loginPassword(loginPassword)
                .phoneNumber(phoneNumber)
                .gender(Gender.findByName(gender))
                .major(Major.findByDepartment(department))
                .build();
    }
}
