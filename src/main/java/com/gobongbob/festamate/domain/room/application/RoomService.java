package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_PARTICIPATING;
import static com.gobongbob.festamate.global.response.ResponseCode.CAN_NOT_UPDATE;
import static com.gobongbob.festamate.global.response.ResponseCode.MUST_HOST;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Role;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomParticipantRepository;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final MessageRepository messageRepository;
    private final ImageService imageService;

    @Transactional
    public ChatRoom createRoom(Member member, RoomCreateRequest request, List<MultipartFile> imageFiles) {
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

        return chatRoom;
    }

    public Slice<RoomListResponse> findBySearchCondition(Pageable pageable, FilteringCondition filteringCondition) {
        return roomRepository.findBySearchCondition(pageable, filteringCondition)
                .map(room -> RoomListResponse.fromEntity(
                        room,
                        roomParticipantRepository.countByRoom_Id(room.getId())
                ));
    }

    public List<RoomListResponse> findParticipatingRooms(Long memberId) {
        return roomParticipantRepository.findByMember_Id(memberId)
                .stream()
                .map(roomParticipant -> RoomListResponse.fromEntity(
                        roomParticipant.getRoom(),
                        roomParticipantRepository.countByRoom_Id(roomParticipant.getRoom().getId())
                )).toList();
    }

    public RoomResponse findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> RoomResponse.fromEntity(
                        room,
                        roomParticipantRepository.findByRoomAndRole(room.getId(), Role.HOST),
                        roomParticipantRepository.findByRoomAndRole(room.getId(), Role.GUEST)
                )).orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
    }

    @Transactional
    public void updateRoomById(Member member, Long roomId, RoomUpdateRequest request, List<MultipartFile> imageFiles) {
        Room room = roomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateIsHost(room, member);
        validateAlone(room);

        room.updateRoom(
                request.title(),
                request.place(),
                request.content(),
                request.preferredGender(),
                request.preferredStudentIdMin(),
                request.preferredStudentIdMax(),
                request.meetingDateTime(),
                request.maxParticipants()
        );
    }

    // 방 삭제(일반, admin)
    @Transactional
    public void deleteRoomById(Member member, Long roomId) {
        Room room = roomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateIsHost(room, member);

        roomParticipantRepository.deleteByRoomId(roomId);
        messageRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteByRoomId(roomId);
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
                    throw new BadRequestException(ALREADY_PARTICIPATING);
                });
    }

    private void validateIsHost(Room room, Member member) {
        if (!member.isHost(room)) {
            throw new BadRequestException(MUST_HOST);
        }
    }

    private void validateAlone(Room room) {
        int participantsCount = roomParticipantRepository.countByRoom_Id(room.getId());
        if (participantsCount > 1) {
            throw new BadRequestException(CAN_NOT_UPDATE);
        }
    }
}
