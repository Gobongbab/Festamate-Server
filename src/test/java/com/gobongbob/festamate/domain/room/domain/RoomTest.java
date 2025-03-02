package com.gobongbob.festamate.domain.room.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gobongbob.festamate.common.fixture.MemberFixture;
import com.gobongbob.festamate.common.fixture.RoomFixture;
import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RoomTest")
class RoomTest {

    @Test
    @DisplayName("모임방 객체를 수정한다")
    void updateRoom() {
        // given
        Member member = MemberFixture.createMember(
                "testNickname1",
                "202500001",
                "testLoginId1",
                "01012345678",
                Major.COMPUTER_SCIENCE
        );

        Room room = RoomFixture.createRoom(4, Gender.MALE, member);

        // when
        room.updateRoom(
                6,
                Gender.FEMALE,
                room.getOpenChatLink(),
                room.getMeetingDateTime(),
                room.getTitle(),
                room.getContent()
        );

        // then
        assertThat(room.getHeadCount()).isEqualTo(6);
        assertThat(room.getPreferredGender()).isEqualTo(Gender.FEMALE);
    }

    /*
    다음 테스트 케이스들이 추가로 작성되어야 합니다.

    - 방장이 아닌 사용자가 모임방 정보를 수정하려는 경우, 예외를 발생시킨다.
    - 방장 외의 인원이 입장한 방의 정보를 수정하려는 경우, 예외를 발생시킨다.

    이 외에도 전체 인원을 짝수로 통일할 것인지, 남성과 여성을 동일한 비율로 맞출 것인지 등에 대한 논의 후
    테스트 케이스가 작성되어야 합니다.
     */
}