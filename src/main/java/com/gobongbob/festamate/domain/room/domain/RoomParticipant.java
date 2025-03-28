package com.gobongbob.festamate.domain.room.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private boolean isHost;

    public static RoomParticipant createHost(Room room, Member member) {
        return RoomParticipant.builder()
                .room(room)
                .member(member)
                .isHost(true)
                .build();
    }

    public static RoomParticipant createParticipant(Room room, Member member) {
        return RoomParticipant.builder()
                .room(room)
                .member(member)
                .isHost(false)
                .build();
    }
}
