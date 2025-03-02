package com.gobongbob.festamate.common.fixture;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;

public class MemberFixture {

    public static Member createMember(
            String nickname,
            String studentId,
            String loginId,
            String phoneNumber,
            Major major
    ) {
        return Member.builder()
                .name("testName1")
                .nickname(nickname)
                .studentId(studentId)
                .loginId(loginId)
                .loginPassword("testPassword1")
                .phoneNumber(phoneNumber)
                .gender(Gender.MALE)
                .major(major)
                .build();
    }

    public static Member MEMBER1() {
        return MemberFixture.createMember(
                "testNickname1",
                "202500001",
                "testLoginId1",
                "01012345678",
                Major.COMPUTER_SCIENCE
        );
    }
}
