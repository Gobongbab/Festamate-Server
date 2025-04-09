package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_PARTICIPATING;
import static com.gobongbob.festamate.global.response.ResponseCode.MUST_NORMAL;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_PARTICIPATING_ROOM;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Role;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.dto.response.IsMemberHostResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.domain.room.presentation.RoomParticipantRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
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
    public void participateAlone(Member member, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));

//        validateRoomParticipation(member.getId());
        validateRoomFull(room.getId(), 1);

        RoomParticipant roomParticipant = RoomParticipant.createParticipant(room, member, Role.GUEST);
        roomParticipantRepository.save(roomParticipant);
        member.useTicket();
    }

    @Transactional
    public void leave(Member member, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateNotHost(room, member);

        roomParticipantRepository.deleteByMember_Id(member.getId());
    }

    public IsMemberHostResponse isMemberHost(Long roomId, Member member) {
        boolean isHost = roomParticipantRepository.findByRoom_IdAndMember_Id(roomId, member.getId())
                .orElseThrow(() -> new BadRequestException(NO_PARTICIPATING_ROOM))
                .isHost();

        return new IsMemberHostResponse(isHost);
    }

    private void validateRoomParticipation(Long memberId) {
        roomParticipantRepository.findByMember_Id(memberId)
                .stream()
                .findFirst()
                .ifPresent(roomParticipant -> {
                    throw new BadRequestException(ALREADY_PARTICIPATING);
                });
    }

    private void validateRoomFull(Long roomId, int membersToParticipate) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));

        if (isRoomFull(membersToParticipate, room)) {
            throw new BadRequestException(NOT_FOUND_ROOM);
        }
    }

    private boolean isRoomFull(int membersToParticipate, Room room) {
        int currentParticipants = roomParticipantRepository.countByRoom_Id(room.getId());

        return membersToParticipate + currentParticipants > room.getMaxParticipants();
    }

    private void validateNotHost(Room room, Member member) {
        if (member.isHost(room)) {
            throw new BadRequestException(MUST_NORMAL);
        }
    }
}
