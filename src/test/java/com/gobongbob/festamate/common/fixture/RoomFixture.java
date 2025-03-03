package com.gobongbob.festamate.common.fixture;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import java.time.LocalDateTime;
import java.util.List;

public class RoomFixture {

    public static Room createRoom(int headCount, Gender gender, Member member) {
        return Room.builder()
                .headCount(headCount)
                .preferredGender(gender)
                .openChatLink("https://open.kakao.com/o/test")
                .meetingDateTime(LocalDateTime.now())
                .title("test title")
                .content("test content")
                .host(member)
                .build();
    }

    public static List<Room> createRooms(Member member) {
        return List.of(
                ROOM1(member),
                ROOM2(member),
                ROOM3(member)
        );
    }

    public static RoomCreateRequest createRoomCreateRequest(Room room) {
        return new RoomCreateRequest(
                room.getHeadCount(),
                room.getPreferredGender().getName(),
                room.getOpenChatLink(),
                room.getMeetingDateTime(),
                room.getTitle(),
                room.getContent()
        );
    }

    public static Room ROOM1(Member member) {
        return createRoom(4, Gender.MALE, member);
    }

    public static Room ROOM2(Member member) {
        return createRoom(6, Gender.FEMALE, member);
    }

    public static Room ROOM3(Member member) {
        return createRoom(6, Gender.MALE, member);
    }
}
