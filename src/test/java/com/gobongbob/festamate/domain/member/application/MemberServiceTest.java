package com.gobongbob.festamate.domain.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.gobongbob.festamate.common.fixture.MemberFixture;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
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
            Member member = MemberFixture.MEMBER1();
            MemberCreateRequest request = MemberFixture.createMemberCreateRequest(member);

            // when
            Member createdMember = memberService.createMember(request);

            // then
            assertAll(
                    () -> assertThat(createdMember.getId()).isNotNull(),
                    () -> assertThat(createdMember.getNickname()).isEqualTo(member.getNickname()),
                    () -> assertThat(createdMember.getStudentId()).isEqualTo(member.getStudentId()),
                    () -> assertThat(createdMember.getLoginId()).isEqualTo(member.getLoginId()),
                    () -> assertThat(createdMember.getPhoneNumber()).isEqualTo(
                            member.getPhoneNumber())
            );
        }
    }

    @Nested
    @DisplayName("회원을 조회할 시")
    class findMember {

        @Test
        @DisplayName("특정 회원 단건 조회에 성공한다.")
        void successFindMemberById() {
            // given
            Member member = testFixtureBuilder.buildMember(MemberFixture.MEMBER1());

            // when
            MemberResponse response = memberService.findMemberById(member.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isNotNull(),
                    () -> assertThat(response.name()).isEqualTo(member.getName()),
                    () -> assertThat(response.nickname()).isEqualTo(member.getNickname()),
                    () -> assertThat(response.studentId()).isEqualTo(member.getStudentId()),
                    () -> assertThat(response.loginId()).isEqualTo(member.getLoginId()),
                    () -> assertThat(response.loginPassword()).isEqualTo(member.getLoginPassword()),
                    () -> assertThat(response.phoneNumber()).isEqualTo(member.getPhoneNumber()),
                    () -> assertThat(response.gender()).isEqualTo(member.getGender().getName()),
                    () -> assertThat(response.major()).isEqualTo(member.getMajor().getDepartment())
            );
        }
    }
}
