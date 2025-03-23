package com.gobongbob.festamate.domain.room.application;

import com.gobongbob.festamate.domain.chatRoom.domain.ChatRoom;
import com.gobongbob.festamate.domain.chatRoom.repository.ChatRoomRepository;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.domain.room.presentation.RoomParticipantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Room createRoom(Member member, RoomCreateRequest request) {
        validateRoomParticipation(member.getId());
        Room createdRoom = roomRepository.save(request.toEntity(member));

        ChatRoom chatRoom = ChatRoom.builder()
                .name(createdRoom.getTitle())
                .room(createdRoom)
                .build();
        chatRoomRepository.save(chatRoom);

        RoomParticipant roomParticipant = RoomParticipant.createHost(createdRoom, member);
        roomParticipantRepository.save(roomParticipant);

        return createdRoom;
    }

    public List<RoomResponse> findAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(room -> {
                    List<RoomParticipant> roomParticipants = roomParticipantRepository.findByRoom_Id(room.getId());
                    return RoomResponse.fromEntity(room, roomParticipants);
                })
                .toList();
    }

    public RoomResponse findParticipatingRooms(Long memberId) {
        Room participatingRoom = roomParticipantRepository.findByRoom_Id(memberId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("참여중인 모임방이 존재하지 않습니다."))
                .getRoom();
        List<RoomParticipant> roomParticipants = roomParticipantRepository.findByRoom_Id(participatingRoom.getId());

        return RoomResponse.fromEntity(participatingRoom, roomParticipants);
    }

    public RoomResponse findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    List<RoomParticipant> roomParticipants = roomParticipantRepository.findByRoom_Id(room.getId());
                    return RoomResponse.fromEntity(room, roomParticipants);
                }).orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
    }

    @Transactional
    public void updateRoomById(Member member, Long roomId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
        validateIsHost(room, member);
        validateAlone(room);

        room.updateRoom(
                request.headCount(),
                Gender.findByName(request.preferredGender()),
                request.openChatLink(),
                request.meetingDateTime(),
                request.title(),
                request.content()
        );
    }

    @Transactional
    public void deleteRoomById(Member member, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
        validateIsHost(room, member);

        roomParticipantRepository.deleteByRoom(room);
        roomRepository.delete(room);

        /*
        추후 방 삭제 시, 방에 참여중인 사용자들에게 알림을 보내는 로직 추가 필요
         */
    }

    private void validateRoomParticipation(Long memberId) {
        roomParticipantRepository.findByMember_Id(memberId)
                .stream()
                .findFirst()
                .ifPresent(roomParticipant -> {
                    throw new IllegalArgumentException("이미 모임방에 참여하고 있습니다.");
                });
    }

    private void validateIsHost(Room room, Member member) {
        if (!member.isHost(room)) {
            throw new IllegalArgumentException("방장이어야 합니다.");
        }
    }

    private void validateAlone(Room room) {
        int participantsCount = roomParticipantRepository.countByRoom_Id(room.getId());
        if (participantsCount > 1) {
            throw new IllegalArgumentException("방에 방장을 제외한 다른 사용자가 입장한 상태에서는 수정할 수 없습니다.");
        }
    }
}
