package com.gobongbob.festamate.domain.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.gobongbob.festamate.common.fixture.MemberFixture;
import com.gobongbob.festamate.common.fixture.RoomFixture;
import com.gobongbob.festamate.common.fixture.RoomParticipantFixture;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.serviceSliceTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Import(RoomServiceTest.TestConfig.class)
@DisplayName("RoomServiceTest")
class RoomServiceTest extends serviceSliceTest {

    @Autowired
    RoomService roomService;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ImageService imageService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ImageService imageService() {
            return Mockito.mock(ImageService.class);
        }
    }

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

            MultipartFile imageFile = new MockMultipartFile(
                    "imageFiles",
                    "test-image.jpg",
                    "image/jpeg",
                    "fake-image-content".getBytes()
            );
            List<MultipartFile> imageFiles = List.of(imageFile);
            doNothing().when(imageService).uploadImages(any());

            // when
            Room createdRoom = roomService.createRoom(member, request, imageFiles).getRoom();

            // then
            assertAll(
                    () -> assertThat(createdRoom.getId()).isNotNull(),
                    () -> assertThat(createdRoom.getMaxParticipants()).isEqualTo(room.getMaxParticipants()),
                    () -> assertThat(createdRoom.getPreferredGender()).isEqualTo(
                            room.getPreferredGender())
            );
            verify(imageService, times(1)).uploadImages(any());
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

            Pageable pageable = PageRequest.of(0, 10);
            FilteringCondition filteringCondition = new FilteringCondition(
                    Status.MATCHING,
                    Gender.MALE,
                    4,
                    "20"
            );

            // when
            Slice<RoomListResponse> findRoomResponses = roomService.findBySearchCondition(pageable, filteringCondition);

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

            int maxParticipantsToUpdate = room.getMaxParticipants() + 4;
            Gender preferredGenderToUpdate = room.getPreferredGender();

            // when
            RoomUpdateRequest request = new RoomUpdateRequest(
                    room.getTitle(),
                    room.getPlace(),
                    room.getContent(),
                    preferredGenderToUpdate.getName(),
                    room.getMeetingDateTime(),
                    maxParticipantsToUpdate
            );
            roomService.updateRoomById(member, room.getId(), request);

            // then
            assertAll(
                    () -> assertThat(room.getMaxParticipants()).isEqualTo(maxParticipantsToUpdate),
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
