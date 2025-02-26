package com.gobongbob.festamate.domain.room.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.fixture.RoomFixture;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.serviceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("모임방 관련 기능 테스트")
class RoomServiceTest extends serviceSliceTest {

    @Autowired
    RoomService roomService;
    @Autowired
    RoomRepository roomRepository;

    @Nested
    @DisplayName("모임방을 생성할 시")
    class createRoom {

        @Test
        @Transactional
        @DisplayName("모임방을 생성에 성공한다.")
        void successCreateRoom() {
            // given
            Room room = RoomFixture.createRoom(4, Gender.MALE);
            RoomCreateRequest request = new RoomCreateRequest(
                    room.getHeadCount(),
                    room.getPreferredGender().getName(),
                    room.getOpenChatLink(),
                    room.getMeetingDateTime(),
                    room.getTitle(),
                    room.getContent()
            );

            // when
            roomService.createRoom(request);
            Room findRoom = roomRepository.findById(room.getId())
                    .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));

            // then
            assertThat(findRoom.getHeadCount()).isEqualTo(room.getHeadCount());
            assertThat(findRoom.getPreferredGender()).isEqualTo(room.getPreferredGender());
        }
    }
}
