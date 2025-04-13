package com.gobongbob.festamate.common.fixture;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Role;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import java.util.List;

public class RoomParticipantFixture {

    public static RoomParticipant createHost(Room room, Member member) {
        return RoomParticipant.createHost(room, member);
    }

    public static RoomParticipant createParticipant(Room room, Member member, Role role) {
        return RoomParticipant.createParticipant(room, member, role);
    }

    public static List<RoomParticipant> createParticipants(Room room) {
        return List.of(
                ROOM_PARTICIPANT_1(room),
                ROOM_PARTICIPANT_2(room)
        );
    }

    public static RoomParticipant ROOM_PARTICIPANT_1(Room room) {
        return createParticipant(room, MemberFixture.MEMBER2(), Role.HOST);
    }

    public static RoomParticipant ROOM_PARTICIPANT_2(Room room) {
        return createParticipant(room, MemberFixture.MEMBER3(), Role.GUEST);
    }
}
