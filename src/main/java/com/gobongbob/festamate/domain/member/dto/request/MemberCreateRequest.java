package com.gobongbob.festamate.domain.member.dto.request;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;

public record MemberCreateRequest(
        String name,
        String nickname,
        String studentId,
        String loginId,
        String loginPassword,
        String phoneNumber,
        String gender
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
                .build();
    }
}
