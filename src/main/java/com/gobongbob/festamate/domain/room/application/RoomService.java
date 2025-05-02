package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.*;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
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
import com.gobongbob.festamate.global.NotificationService;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.List;
import java.util.UUID;
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
    private final RoomImagePicker roomImagePicker;
    private final RoomParticipantRepository roomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    // 방 생성
    @Transactional
    @CheckActiveUser // 메서드 실행 전 현재 사용자가 ACTIVE 상태인지 AOP로 확인 (BLOCKED 시 AccessDeniedException 발생)
    public ChatRoom createRoom(Long memberId, RoomCreateRequest request, List<MultipartFile> imageFiles) {
        Member member = memberRepository.findById(memberId) // 티켓 소모를 위해 영속성 컨텍스트에서 관리하는 member 객체를 재조회
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

        Room createdRoom = roomRepository.save(request.toEntity(member));
        uploadImageIfExist(imageFiles, createdRoom);

        ChatRoom chatRoom = ChatRoom.createChatRoom(createdRoom.getTitle(), createdRoom);
        chatRoomRepository.save(chatRoom);

        RoomParticipant roomParticipant = RoomParticipant.createHost(createdRoom, member);
        roomParticipantRepository.save(roomParticipant);
        member.useTicket();

        // **FCM 알림 전송**, 추후 삭제해야 함.
        String fcmToken = member.getFcmToken(); // FCM 토큰 가져오기
        if (fcmToken != null) {
            notificationService.sendNotification(
                    fcmToken,
                    "방 생성 완료",
                    "방이 성공적으로 생성되었습니다: " + createdRoom.getTitle()
            );
        }

        return chatRoom;
    }

    // 방 전체 조회
    public Slice<RoomListResponse> findBySearchCondition(Pageable pageable, FilteringCondition filteringCondition) {
        return roomRepository.findBySearchCondition(pageable, filteringCondition)
                .map(RoomListResponse::fromEntity);
    }

    // 참여 중인 모임방 조회
    @CheckActiveUser
    public List<RoomListResponse> findParticipatingRooms(Long memberId) {
        return roomParticipantRepository.findByMember_Id(memberId)
                .stream()
                .map(RoomParticipant::getRoom)
                .map(RoomListResponse::fromEntity)
                .toList();
    }

    public RoomResponse findRoomById(CustomMemberDetails memberDetails, Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> RoomResponse.fromEntity(
                        room,
                        findRoomAuthorityByMember(room, memberDetails),
                        roomParticipantRepository.findByRoomAndRole(room.getId(), ParticipantRole.HOST),
                        roomParticipantRepository.findByRoomAndRole(room.getId(), ParticipantRole.GUEST)
                )).orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
    }

    // 모임방 정보 수정
    @Transactional
    @CheckActiveUser
    public void updateRoomById(Member member, Long roomId, RoomUpdateRequest request, List<MultipartFile> imageFiles) {
        Room room = roomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateIsHost(room, member);
        validateAlone(room);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            room.getImages().forEach(roomImage -> imageService.delete(roomImage.getImage()));
            room.getImages().clear();

            List<RoomImage> roomImages = imageService.uploadImages(imageFiles)
                    .stream()
                    .map(RoomImage::fromEntity)
                    .toList();
            room.assignImages(roomImages);
        }

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
        room.getChatRoom().updateTitle(request.title());
    }

    // 방 삭제(일반, admin)
    @Transactional
    @CheckActiveUser
    public void deleteRoomById(Member member, Long roomId) {
        Room room = roomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateIsHost(room, member);

        messageRepository.deleteByRoomId(roomId);
        roomRepository.delete(room);

        /*
        추후 방 삭제 시, 방에 참여중인 사용자들에게 알림을 보내는 로직 추가 필요
         */
    }

    private RoomAuthority findRoomAuthorityByMember(Room room, CustomMemberDetails memberDetails) {
        if (memberDetails == null) {
            return RoomAuthority.NON_MEMBER;
        }

        return room.getParticipants()
                .stream()
                .filter(participant -> participant.getMember().getId().equals(memberDetails.getMember().getId()))
                .findFirst()
                .map(participant -> participant.isHost() ? RoomAuthority.HOST : RoomAuthority.PARTICIPANT)
                .orElse(RoomAuthority.NON_PARTICIPANT);
    }

    private void uploadImageIfExist(List<MultipartFile> imageFiles, Room createdRoom) {
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<RoomImage> roomImages = imageService.uploadImages(imageFiles)
                    .stream()
                    .map(RoomImage::fromEntity)
                    .toList();
            createdRoom.assignImages(roomImages);
        }
        if (imageFiles == null || imageFiles.isEmpty()) {
            Image image = pickRandomImage();
            RoomImage roomImage = RoomImage.fromEntity(image);
            createdRoom.assignImages(List.of(roomImage));
        }
    }

    private Image pickRandomImage() {
        String randomImageUrl = roomImagePicker.getRandomImageUrl();

        return Image.builder()
                .url(randomImageUrl)
                .uploadName(UUID.randomUUID().toString())
                .storeName(UUID.randomUUID().toString())
                .build();
    }

    // chatService에 있는 validateRoomParticipation와 중복됩니다. 이 부분 확인 부탁드려요!
    private void validateRoomParticipation(Long memberId) {
        roomParticipantRepository.findByMember_Id(memberId)
                .stream()
                .findFirst()
                .ifPresent(roomParticipant -> {
                    throw new BadRequestException(ALREADY_PARTICIPATING);
                });
    }

    private void validateIsHost(Room room, Member member) {
        if (!member.isHost(room) && !member.isAdmin()) {
            throw new BadRequestException(MUST_HOST);
        }
    }

    private void validateAlone(Room room) {
        if (!room.isJoinable()) { // 호스트 측 참가자만 있는 경우
            throw new BadRequestException(CAN_NOT_UPDATE);
        }
    }
}
