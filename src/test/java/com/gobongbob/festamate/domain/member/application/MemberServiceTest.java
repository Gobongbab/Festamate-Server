package com.gobongbob.festamate.domain.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.fixture.MemberFixture;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.serviceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("MemberServiceTest")
class MemberServiceTest extends serviceSliceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Nested
    @DisplayName("회원을 생성할 시")
    class createMember {

        @Test
        @Transactional
        @DisplayName("회원 생성에 성공한다.")
        void successCreateRoom() {
            // given
            Member member = MemberFixture.createMember(
                    "testNickname1",
                    "202500001",
                    "testLoginId1",
                    "01012345678",
                    Major.COMPUTER_SCIENCE
            );
            MemberCreateRequest request = new MemberCreateRequest(
                    member.getName(),
                    member.getNickname(),
                    member.getStudentId(),
                    member.getLoginId(),
                    member.getLoginPassword(),
                    member.getPhoneNumber(),
                    member.getGender().getName(),
                    member.getMajor().getCollege(),
                    member.getMajor().getDepartment()
            );

            // when
            Member createdMember = memberService.createMember(request);

            // then
            assertThat(createdMember.getNickname()).isEqualTo(member.getNickname());
            assertThat(createdMember.getStudentId()).isEqualTo(member.getStudentId());
            assertThat(createdMember.getLoginId()).isEqualTo(member.getLoginId());
            assertThat(createdMember.getPhoneNumber()).isEqualTo(member.getPhoneNumber());
        }
    }
}