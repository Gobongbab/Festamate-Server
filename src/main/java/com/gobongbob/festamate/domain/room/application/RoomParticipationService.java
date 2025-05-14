package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_MATCHED;
import static com.gobongbob.festamate.global.response.ResponseCode.GENDER_NOT_MATCH;
import static com.gobongbob.festamate.global.response.ResponseCode.MUST_NORMAL;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_PARTICIPATING_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.PHONE_NUMBER_DUPLICATE;
import static com.gobongbob.festamate.global.response.ResponseCode.ROOM_FULL;
import static com.gobongbob.festamate.global.response.ResponseCode.ROOM_NOT_JOINABLE;
import static com.gobongbob.festamate.global.response.ResponseCode.STUDENT_ID_NOT_MATCH;

import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.ParticipantRole;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.dto.request.FriendPhoneNumbersRequest;
import com.gobongbob.festamate.domain.room.dto.response.IsMemberHostResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomParticipantRepository;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.global.NotificationService;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomParticipationService {

    private final RoomRepository roomRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    // 방 참여
    @Transactional
    @CheckActiveUser
    public Room participate(Long memberId, Long roomId, FriendPhoneNumbersRequest request) {
        Member member = memberRepository.findById(memberId) // 티켓 소모를 위해 영속성 컨텍스트에서 관리하는 member 객체를 재조회
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        List<Member> participants = collectParticipants(member, request);

        validateParticipation(room, participants);

        participants.forEach(participant -> participateRoom(participant, room));
        if (room.isFull()) { // 방에 참여자가 다 찼을 때
            room.updateStatus(Status.MATCHED);
//            sendNotifications(room);
        }

        return room;
    }

    // 모임방 나가기
    @Transactional
    @CheckActiveUser
    public Room leave(Member member, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateNotHost(room, member);
        validateRoomMatching(room);

        if (!member.isHost(room)) { // 참가자 측이 방을 나가면 참가자만 삭제
            room.getParticipants().removeIf(participant -> participant.getParticipantRole() == ParticipantRole.GUEST);
        }
        if (member.isHost(room)) { // 방장이 방을 나가면 방을 삭제
            messageRepository.deleteByRoomId(roomId);
            roomRepository.delete(room);
        }

        return room;
    }

    // 방장 여부 확인
    @CheckActiveUser
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
        RoomParticipant roomParticipant = RoomParticipant.createParticipant(room, member, ParticipantRole.GUEST);
        roomParticipantRepository.save(roomParticipant);
    }

    private List<Member> collectParticipants(Member member, FriendPhoneNumbersRequest request) {
        List<Member> members = request.friendPhoneNumbers()
                .stream()
                .map(phoneNumber -> memberRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new BadRequestException(NO_MEMBER))
                ).collect(Collectors.toList());
        members.add(member);

        return members;
    }

//    private void sendNotifications(Room room) {
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//            @Override
//            public void afterCommit() {
//                room.getParticipants().forEach(participant -> {
//                    String fcmToken = participant.getMember().getFcmToken();
//                    Long participantId = participant.getMember().getId();
//                    if (fcmToken != null) {
//                        notificationService.sendNotification(
//                                fcmToken,
//                                "방 매칭 완료",
//                                "방 매칭이 완료되었습니다: " + room.getTitle(),
//                                participantId
//                        );
//                    }
//                });
//            }
//        });
//    }

    private void validateParticipation(Room room, List<Member> participants) {
        validateRoomMatching(room); // 현재 매칭중인 방인지 확인
        validateRoomFull(room); // 방에 참여자가 다 찼는지 확인
        validateRoomJoinable(room); // 방에 참여할 수 있는 인원인지 확인
        validatePhoneNumberUnique(participants); // 참여자 간의 전화번호가 중복되지 않는지 확인
        validateGender(room, participants); // 방의 성별과 참여자의 성별이 일치하는지 확인
        validateStudentId(room, participants); // 방의 학번과 참여자의 학번이 일치하는지 확인
    }

    private void validateRoomMatching(Room room) {
        if (room.getStatus() == Status.MATCHED) {
            throw new BadRequestException(ALREADY_MATCHED);
        }
    }

    private void validateRoomFull(Room room) {
        if (room.isFull()) {
            throw new BadRequestException(ROOM_FULL);
        }
    }

    private void validateRoomJoinable(Room room) {
        if (!room.isJoinable()) { // 모임의 남은 자리 수와 참여하려는 인원의 수가 맞는지
            throw new BadRequestException(ROOM_NOT_JOINABLE);
        }
    }

    private void validatePhoneNumberUnique(List<Member> participants) {
        int phoneNumberCount = (int) participants.stream()
                .map(Member::getPhoneNumber)
                .distinct()
                .count();

        if (phoneNumberCount != participants.size()) {
            throw new BadRequestException(PHONE_NUMBER_DUPLICATE);
        }
    }

    private void validateGender(Room room, List<Member> participants) {
        boolean hasMismatchedGender = participants.stream()
                .map(Member::getGender)
                .anyMatch(gender -> gender != room.getPreferredGender());

        if (hasMismatchedGender) {
            throw new BadRequestException(GENDER_NOT_MATCH);
        }
    }

    private void validateStudentId(Room room, List<Member> participants) {
        boolean hasMismatchedStudentId = participants.stream()
                .map(member -> member.getStudentId().substring(2, 4))
                .anyMatch(studentId -> Integer.parseInt(studentId) < room.getPreferredStudentIdMin()
                        || Integer.parseInt(studentId) > room.getPreferredStudentIdMax());

        if (hasMismatchedStudentId) {
            throw new BadRequestException(STUDENT_ID_NOT_MATCH);
        }
    }

    private void validateNotHost(Room room, Member member) {
        if (member.isHost(room)) {
            throw new BadRequestException(MUST_NORMAL);
        }
    }
}
