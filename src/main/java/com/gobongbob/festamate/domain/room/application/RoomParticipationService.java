package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_MATCHED;
import static com.gobongbob.festamate.global.response.ResponseCode.MUST_NORMAL;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_PARTICIPATING_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.PHONE_NUMBER_DUPLICATE;
import static com.gobongbob.festamate.global.response.ResponseCode.ROOM_NOT_JOINABLE;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Role;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.dto.request.FriendPhoneNumbersRequest;
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
    private final ChatRoomRepository chatRoomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoom participate(Long memberId, Long roomId, FriendPhoneNumbersRequest request) {
        Member member = memberRepository.findById(memberId) // 티켓 소모를 위해 영속성 컨텍스트에서 관리하는 member 객체를 재조회
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));

        validateRoomMatching(room); // 현재 매칭중인 방인지 확인
        validateRoomJoinable(room, request.friendPhoneNumbers().size() + 1); // 방에 참여할 수 있는 인원인지 확인
        validatePhoneNumberUnique(member, request.friendPhoneNumbers()); // 참여자 간의 전화번호가 중복되지 않는지 확인

        participateRoom(member, room);
        if (!request.friendPhoneNumbers().isEmpty()) { // 친구와 함께 참여
            participateRoomForFriends(room, request);
        }

        return chatRoomRepository.findByRoom(room)
                .orElseThrow(() -> new BadRequestException(CHAT_ROOM_NOT_FOUND));
    }

    @Transactional
    public ChatRoom leave(Member member, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateNotHost(room, member);

        List<RoomParticipant> guestParticipants = roomParticipantRepository.findByRoomAndRole(roomId, Role.GUEST);
        roomParticipantRepository.deleteAll(guestParticipants);

        return chatRoomRepository.findByRoom(room)
                .orElseThrow(() -> new BadRequestException(CHAT_ROOM_NOT_FOUND));
    }

    public IsMemberHostResponse isMemberHost(Long roomId, Member member) {
        boolean isHost = roomParticipantRepository.findByRoom_IdAndMember_Id(roomId, member.getId())
                .orElseThrow(() -> new BadRequestException(NO_PARTICIPATING_ROOM))
                .isHost();

        return new IsMemberHostResponse(isHost);
    }

    private void participateRoomForFriends(Room room, FriendPhoneNumbersRequest request) {
        request.friendPhoneNumbers()
                .stream()
                .map(phoneNumber -> memberRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new BadRequestException(NO_MEMBER)))
                .forEach(participant -> participateRoom(participant, room));
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
        if (guestParticipants != participants) {
            throw new BadRequestException(ROOM_NOT_JOINABLE);
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
