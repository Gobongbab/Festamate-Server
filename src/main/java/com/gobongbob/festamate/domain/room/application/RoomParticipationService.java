package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_MATCHED;
import static com.gobongbob.festamate.global.response.ResponseCode.MUST_NORMAL;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_PARTICIPATING_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.PHONE_NUMBER_DUPLICATE;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Role;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.dto.request.ParticipationWithFriendRequest;
import com.gobongbob.festamate.domain.room.dto.response.IsMemberHostResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomParticipantRepository;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public void participateWithFriends(Member member, Long roomId, ParticipationWithFriendRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateRoomFull(room.getId(), request.friendPhoneNumbers().size() + 1);

        List<Member> participants = findParticipantsWithPhoneNumber(request);
        participants.add(member);

        participants.stream()
                .map(participant -> RoomParticipant.createParticipant(room, participant, Role.GUEST))
                .forEach(participant -> {
                    roomParticipantRepository.save(participant);
                    participant.getMember().useTicket();
                });
    }

    @Transactional
    public void leave(Member member, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateNotHost(room, member);

        List<RoomParticipant> guestParticipants = roomParticipantRepository.findByRoomAndRole(roomId, Role.GUEST);
        roomParticipantRepository.deleteAll(guestParticipants);
    }

    public IsMemberHostResponse isMemberHost(Long roomId, Member member) {
        boolean isHost = roomParticipantRepository.findByRoom_IdAndMember_Id(roomId, member.getId())
                .orElseThrow(() -> new BadRequestException(NO_PARTICIPATING_ROOM))
                .isHost();

        return new IsMemberHostResponse(isHost);
    }

    private List<Member> findParticipantsWithPhoneNumber(ParticipationWithFriendRequest request) {
        return request.friendPhoneNumbers()
                .stream()
                .map(phoneNumber -> memberRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new BadRequestException(NO_MEMBER)))
                .toList();
    }

    private void participateRoom(Member member, Room room) {
        member.useTicket();
        RoomParticipant roomParticipant = RoomParticipant.createParticipant(room, member, Role.GUEST);
        roomParticipantRepository.save(roomParticipant);

    }

    private void validateRoomMatching(Room room) {
        if (room.getStatus() == Status.MATCHED) {
            throw new BadRequestException(ALREADY_MATCHED);
        }
    }

    private void validateRoomJoinable(Room room, int participants) {
        int guestParticipants = room.getMaxParticipants() / 2;
        if (guestParticipants == participants) {
            throw new BadRequestException(NOT_FOUND_ROOM);
        }
    }

    private void validatePhoneNumberUnique(Member member, List<String> phoneNumbers) {
        Set<String> participantPhoneNumbers = new HashSet<>(phoneNumbers);
        participantPhoneNumbers.add(member.getPhoneNumber());

        if (participantPhoneNumbers.size() != phoneNumbers.size() + 1) {
            throw new BadRequestException(PHONE_NUMBER_DUPLICATE);
        }
    }

    private void validateNotHost(Room room, Member member) {
        if (member.isHost(room)) {
            throw new BadRequestException(MUST_NORMAL);
        }
    }
}
