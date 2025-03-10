package com.gobongbob.festamate.domain.room.application;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.domain.room.presentation.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomParticipationService {

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void participateRoom(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        validateRoomParticipation(member.getId());
        validateRoomFull(room.getId());

        RoomParticipant roomParticipant = RoomParticipant.createParticipant(room, member);
        roomParticipantRepository.save(roomParticipant);
    }

    @Transactional
    public void leaveRoomById(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        validateNotHost(room, member);

        roomParticipantRepository.deleteByMember_Id(member.getId());
    }

    public boolean isMemberHost(Long roomId, Long memberId) {
        return roomParticipantRepository.findByRoom_IdAndMember_Id(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("참여중인 모임방이 존재하지 않습니다."))
                .isHost();
    }

    private void validateRoomParticipation(Long memberId) {
        roomParticipantRepository.findByMember_Id(memberId)
                .stream()
                .findFirst()
                .ifPresent(roomParticipant -> {
                    throw new IllegalArgumentException("이미 모임방에 참여하고 있습니다.");
                });
    }

    private void validateRoomFull(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
        int participantsCount = roomParticipantRepository.countByRoom_Id(roomId);

        if (room.getHeadCount() == participantsCount) {
            throw new IllegalArgumentException("모임방이 꽉 찼습니다.");
        }
    }

    private void validateNotHost(Room room, Member member) {
        if (member.isHost(room)) {
            throw new IllegalArgumentException("일반 회원이어야 합니다.");
        }
    }
}
