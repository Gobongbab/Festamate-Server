package com.gobongbob.festamate.domain.room.application;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.domain.roomParticipant.domain.RoomParticipant;
import com.gobongbob.festamate.domain.roomParticipant.persistence.RoomParticipantRepository;
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
    private final MemberRepository memberRepository;

    @Transactional
    public Room createRoom(RoomCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        validateRoomParticipation(member.getId());

        Room createdRoom = roomRepository.save(request.toEntity(member));

        RoomParticipant roomParticipant = RoomParticipant.builder()
                .room(createdRoom)
                .member(member)
                .isHost(true)
                .build();
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

    public RoomResponse findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    List<RoomParticipant> roomParticipants = roomParticipantRepository.findByRoom_Id(room.getId());
                    return RoomResponse.fromEntity(room, roomParticipants);
                }).orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
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

    @Transactional
    public void updateRoomById(Long roomId, Long memberId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        validateIsHost(room, member);

        /*
        모임방 정보를 수정할 수 있는 조건인지 확인하는 로직이 추후 추가되어야 합니다.
        1. 요청한 사용자가 방장인지 확인한다. (완료)
        2. 방에 방장을 제외한 다른 사용자가 입장하지 않은 것을 확인한다.
         */

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
    public void deleteRoomById(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        validateIsHost(room, member);

        /*
        요청한 사용자가 방장인지 확인하는 로직이 추후 추가되어야 합니다. (완료)
        방장이 모임방 구성원들의 동의 없이 모임방을 삭제할 수 있는지에 대한 논의가 필요합니다.
         */

        roomRepository.delete(room);
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

    private void validateIsHost(Room room, Member member) {
        if (!member.isHost(room)) {
            throw new IllegalArgumentException("방장만 모임방 정보를 수정할 수 있습니다.");
        }
    }
}
