package com.gobongbob.festamate.domain.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.gobongbob.festamate.common.fixture.MemberFixture;
import com.gobongbob.festamate.common.fixture.RoomFixture;
import com.gobongbob.festamate.common.fixture.RoomParticipantFixture;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
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
    @Autowired
    MemberRepository memberRepository;

    @Nested
    @DisplayName("모임방을 생성할 시")
    class createRoom {

        @Test
        @Transactional
        @DisplayName("모임방을 생성에 성공한다.")
        void successCreateRoom() {
            // given
            Member member = testFixtureBuilder.buildMember(MemberFixture.MEMBER1());

            Room room = RoomFixture.ROOM1(member);
            RoomCreateRequest request = RoomFixture.createRoomCreateRequest(room);

            // when
            Room createdRoom = roomService.createRoom(member, request);

            // then
            assertAll(
                    () -> assertThat(createdRoom.getId()).isNotNull(),
                    () -> assertThat(createdRoom.getHeadCount()).isEqualTo(room.getHeadCount()),
                    () -> assertThat(createdRoom.getPreferredGender()).isEqualTo(
                            room.getPreferredGender())
            );
        }
    }

    @Nested
    @DisplayName("모임방을 조회할 시")
    class findRoom {

        @Test
        @DisplayName("특정 모임방 단건 조회에 성공한다.")
        void successFindRoomById() {
            // given
            Member member = testFixtureBuilder.buildMember(MemberFixture.MEMBER1());
            Room room = testFixtureBuilder.buildRoom(RoomFixture.ROOM1(member));

            // when
            RoomResponse findRoom = roomService.findRoomById(room.getId());

            // then
            assertThat(room.getId()).isEqualTo(findRoom.id());
        }

        @Test
        @DisplayName("모든 모임방 조회에 성공한다.")
        void successFindAllRooms() {
            // given
            Member member = testFixtureBuilder.buildMember(MemberFixture.MEMBER1());
            List<Room> rooms = RoomFixture.createRooms(member);
            rooms.forEach(room -> testFixtureBuilder.buildRoom(room));

            // when
            List<RoomResponse> findRoomResponses = roomService.findAllRooms();

            // then
            assertThat(findRoomResponses).hasSize(rooms.size());
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
            Member member = testFixtureBuilder.buildMember(MemberFixture.MEMBER1());
            Room room = testFixtureBuilder.buildRoom(RoomFixture.ROOM1(member));
            testFixtureBuilder.buildRoomParticipant(RoomParticipantFixture.createHost(room, member));

            int headCountToUpdate = room.getHeadCount() + 4;
            Gender preferredGenderToUpdate = room.getPreferredGender();

            // when
            RoomUpdateRequest request = new RoomUpdateRequest(
                    headCountToUpdate,
                    preferredGenderToUpdate.getName(),
                    room.getOpenChatLink(),
                    room.getMeetingDateTime(),
                    room.getTitle(),
                    room.getContent()
            );
            roomService.updateRoomById(member, room.getId(), request);

            // then
            assertAll(
                    () -> assertThat(room.getHeadCount()).isEqualTo(headCountToUpdate),
                    () -> assertThat(room.getPreferredGender()).isEqualTo(preferredGenderToUpdate)
            );
        }
    }

    @Nested
    @DisplayName("모임방을 삭제할 시")
    class deleteRoom {

        @Test
        @Transactional
        @DisplayName("모임방을 삭제에 성공한다.")
        void deleteRoomById() {
            // given
            Member member = testFixtureBuilder.buildMember(MemberFixture.MEMBER1());
            Room room = testFixtureBuilder.buildRoom(RoomFixture.ROOM1(member));

            // when
            roomService.deleteRoomById(member, room.getId());

            // then
            Long roomId = room.getId();
            assertThatThrownBy(() -> roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다.")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("모임방이 존재하지 않습니다.");
        }
    }
}
