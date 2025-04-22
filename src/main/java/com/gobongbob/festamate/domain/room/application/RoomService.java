package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.*;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.image.persistence.RoomImageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.ParticipantRole;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomAuthority;
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
    private final RoomImageRepository roomImageRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoom createRoom(Long memberId, RoomCreateRequest request, List<MultipartFile> imageFiles) {
        Member member = memberRepository.findById(memberId) // 티켓 소모를 위해 영속성 컨텍스트에서 관리하는 member 객체를 재조회
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

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
                        roomParticipantRepository.findByRoomAndRole(room.getId(), ParticipantRole.HOST),
                        roomParticipantRepository.findByRoomAndRole(room.getId(), ParticipantRole.GUEST)
                )).orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
    }

    @Transactional
    public void updateRoomById(Member member, Long roomId, RoomUpdateRequest request, List<MultipartFile> imageFiles) {
        Room room = roomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateIsHost(room, member);
        validateAlone(room);

        imageFiles.stream()
                .filter(imageFile -> imageFile == null || imageFile.isEmpty())
                .findAny()
                .ifPresentOrElse(
                        imageFile -> {
                        },
                        () -> {
                            room.getImages()
                                    .forEach(roomImage -> imageService.delete(roomImage.getImage()));
                            room.getImages().clear();

                            List<RoomImage> roomImages = imageService.uploadImages(imageFiles)
                                    .stream()
                                    .map(RoomImage::fromEntity)
                                    .toList();
                            room.assignImages(roomImages);
                        }
                );

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

    private RoomAuthority findRoomAuthorityByMember(Room room, CustomMemberDetails memberDetails) {
        if (memberDetails == null) {
            return RoomAuthority.NON_MEMBER;
        }

        return roomParticipantRepository.findByRoom_IdAndMember_Id(room.getId(), memberDetails.getMember().getId())
                .stream()
                .findFirst()
                .map(participant -> participant.isHost() ? RoomAuthority.HOST : RoomAuthority.PARTICIPANT)
                .orElse(RoomAuthority.NON_PARTICIPANT);
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
        if (!member.isHost(room) || !member.isAdmin()) {
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
