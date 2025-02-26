package com.gobongbob.festamate.domain.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.fixture.RoomFixture;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.serviceSliceTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("RoomServiceTest")
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
            Room createdRoom = roomService.createRoom(request);

            // then
            assertThat(createdRoom.getHeadCount()).isEqualTo(room.getHeadCount());
            assertThat(createdRoom.getPreferredGender()).isEqualTo(room.getPreferredGender());
        }
    }

    @Nested
    @DisplayName("모임방을 조회할 시")
    class findRoom {

        @Test
        @DisplayName("특정 모임방을 단건으로 조회하는 데 성공한다.")
        void successFindRoomById() {
            // given
            Room room = RoomFixture.createRoom(4, Gender.MALE);
            Room savedRoom = roomRepository.save(room);

            // when
            RoomResponse findRoom = roomService.findRoomById(room.getId());

            // then
            assertThat(savedRoom.getId()).isEqualTo(findRoom.id());
        }

        @Test
        @DisplayName("모든 모임방 조회하는 데 성공한다.")
        void successFindAllRooms() {
            // given
            List<RoomCreateRequest> requests = RoomFixture.createRooms()
                    .stream()
                    .map(room -> new RoomCreateRequest(
                                    room.getHeadCount(),
                                    room.getPreferredGender().getName(),
                                    room.getOpenChatLink(),
                                    room.getMeetingDateTime(),
                                    room.getTitle(),
                                    room.getContent()
                            )
                    ).toList();

            // when
            requests.forEach(roomService::createRoom);
            List<RoomResponse> findRoomResponses = roomService.findAllRooms();

            // then
            assertThat(findRoomResponses).hasSize(requests.size());
        }
    }

    @Nested
    @Transactional
    @DisplayName("모임방을 수정할 시")
    class updateRoom {

        @Test
        @DisplayName("수정에 성공한다.")
        void successUpdateRoomById() {
            // given
            Room room = RoomFixture.createRoom(4, Gender.MALE);
            roomRepository.save(room);

            int headCountToUpdate = 8;
            Gender preferredGenderToUpdate = Gender.FEMALE;

            // when
            RoomUpdateRequest request = new RoomUpdateRequest(
                    headCountToUpdate,
                    preferredGenderToUpdate.getName(),
                    room.getOpenChatLink(),
                    room.getMeetingDateTime(),
                    room.getTitle(),
                    room.getContent()
            );
            roomService.updateRoomById(room.getId(), request);

            // then
            assertThat(room.getHeadCount()).isEqualTo(headCountToUpdate);
            assertThat(room.getPreferredGender()).isEqualTo(preferredGenderToUpdate);
        }
    }

    @Nested
    @DisplayName("모임방을 삭제할 시")
    class deleteRoom {

        @Test
        @Transactional
        @DisplayName("특정 모임방을 삭제한다")
        void deleteRoomById() {
            // given
            Room room = RoomFixture.createRoom(4, Gender.MALE);
            roomRepository.save(room);

            // when
            roomService.deleteRoomById(room.getId());

            // then
            Long roomId = room.getId();
            assertThatThrownBy(() -> roomService.findRoomById(roomId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("모임방이 존재하지 않습니다.");
        }
    }
}
