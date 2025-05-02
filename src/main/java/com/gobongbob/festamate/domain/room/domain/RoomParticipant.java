package com.gobongbob.festamate.domain.room.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private ParticipantRole participantRole;

    private boolean isHost;

    public static RoomParticipant createHost(Room room, Member member) {
        RoomParticipant roomParticipant = RoomParticipant.builder()
                .member(member)
                .participantRole(ParticipantRole.HOST)
                .isHost(true)
                .build();
        roomParticipant.setRoom(room);

        return roomParticipant;
    }

    public static RoomParticipant createParticipant(Room room, Member member, ParticipantRole participantRole) {
        RoomParticipant roomParticipant = RoomParticipant.builder()
                .room(room)
                .member(member)
                .participantRole(participantRole)
                .isHost(false)
                .build();
        roomParticipant.setRoom(room);

        return roomParticipant;
    }

    // 연관관계 편의 메서드
    public void setRoom(Room room) {
        this.room = room;
        room.getParticipants().add(this);
    }
}
