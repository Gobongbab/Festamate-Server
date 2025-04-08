package com.gobongbob.festamate.domain.member.dto.request;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.Getter;

// 프로필 등록용 DTO
public record ProfileRegisterRequest(
        String name,
        String nickname,
        String studentId,
        @Getter
        String phoneNumber,
        @Getter
        String phoneVerificationCodes,
        String gender,
        String college,
        String department
) {

    // Member 엔티티로 변환하는 메서드
    public Member toEntity(Member existingMember) {
        existingMember.registerProfile(
                name,
                nickname,
                studentId,
                phoneNumber,
                Gender.findByName(gender),
                Major.findByDepartment(department)
        );

        return existingMember;
    }
}