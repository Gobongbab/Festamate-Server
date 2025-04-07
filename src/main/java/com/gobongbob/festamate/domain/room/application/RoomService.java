package com.gobongbob.festamate.domain.room.application;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.domain.room.presentation.RoomParticipantRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ImageService imageService;

    @Transactional
    public Room createRoom(Member member, RoomCreateRequest request, List<MultipartFile> imageFiles) {
//        validateRoomParticipation(member.getId());

        List<RoomImage> roomImages = new ArrayList<>();
        if (!imageFiles.isEmpty()) {
            roomImages = imageService.uploadImages(imageFiles)
                    .stream()
                    .map(RoomImage::fromEntity)
                    .toList();
        }

        Room createdRoom = roomRepository.save(request.toEntity(member));
        createdRoom.assignImages(roomImages);

        ChatRoom chatRoom = ChatRoom.builder()
                .name(createdRoom.getTitle())
                .room(createdRoom)
                .build();
        chatRoomRepository.save(chatRoom);

        RoomParticipant roomParticipant = RoomParticipant.createHost(createdRoom, member);
        roomParticipantRepository.save(roomParticipant);
        member.useTicket();

        return createdRoom;
    }

    public Page<RoomListResponse> findAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable)
                .map(room -> RoomListResponse.fromEntity(
                        room,
                        roomParticipantRepository.countByRoom_Id(room.getId())
                ));
    }

    public List<RoomListResponse> findParticipatingRooms(Long memberId) {
        return roomParticipantRepository.findByRoom_Id(memberId)
                .stream()
                .map(roomParticipant -> RoomListResponse.fromEntity(
                        roomParticipant.getRoom(),
                        roomParticipantRepository.countByRoom_Id(roomParticipant.getRoom().getId())
                )).toList();
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
                request.title(),
                request.content(),
                Gender.findByName(request.preferredGender()),
                request.meetingDateTime(),
                request.maxParticipants()
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
