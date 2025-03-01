package com.gobongbob.festamate.domain.room.fixture;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomFixture {

    public static Room createRoom(int headCount, Gender gender) {
        return Room.builder()
                .headCount(headCount)
                .preferredGender(gender)
                .openChatLink("https://open.kakao.com/o/test")
                .meetingDateTime(LocalDateTime.now())
                .title("test title")
                .content("test content")
                .build();
    }

    public static List<Room> createRooms() {
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            rooms.add(createRoom(2 * (i + 1), Gender.MALE));
        }

        return rooms;
    }
}
