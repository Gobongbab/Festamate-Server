package com.gobongbob.festamate.common.fixture;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.List;

public class MemberFixture {

    private static Member createMember(
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

    public static List<Member> createMembers() {
        return List.of(
                MEMBER1(),
                MEMBER2(),
                MEMBER3()
        );
    }

    public static Member MEMBER1() {
        return createMember(
                "testNickname1",
                "202500001",
                "testLoginId1",
                "01011111111",
                Major.COMPUTER_SCIENCE
        );
    }

    public static Member MEMBER2() {
        return createMember(
                "testNickname2",
                "202500002",
                "testLoginId2",
                "01022222222",
                Major.BUSINESS_ADMINISTRATION
        );
    }

    public static Member MEMBER3() {
        return createMember(
                "testNickname3",
                "202500003",
                "testLoginId3",
                "01033333333",
                Major.INDUSTRIAL_MANAGEMENT_INFORMATION
        );
    }
}
