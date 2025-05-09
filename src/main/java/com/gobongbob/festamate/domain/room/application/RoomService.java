package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.*;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.ParticipantRole;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomAuthority;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import com.gobongbob.festamate.domain.room.dto.request.FriendPhoneNumbersRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomParticipantRepository;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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

    // 방 생성
    @Transactional
    @CheckActiveUser// 메서드 실행 전 현재 사용자가 ACTIVE 상태인지 AOP로 확인 (BLOCKED 시 AccessDeniedException 발생)
    public ChatRoom createRoom(Long memberId, RoomCreateRequest request, List<MultipartFile> imageFiles) {
        Member hostMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

        Room createdRoom = roomRepository.save(request.toEntity(hostMember));
        uploadImageIfExist(imageFiles, createdRoom);

        ChatRoom chatRoom = ChatRoom.createChatRoom(createdRoom.getTitle(), createdRoom);
        chatRoomRepository.save(chatRoom);

        List<RoomParticipant> participants = collectAndValidateInitialParticipants(request.friendPhoneNumbers(), createdRoom, hostMember);

        participants.forEach(participant -> {
            roomParticipantRepository.save(participant);
            participant.getMember().useTicket(); // 각 참가자의 티켓 사용
        });

        return chatRoom;
    }


    private List<RoomParticipant> collectAndValidateInitialParticipants(FriendPhoneNumbersRequest request, Room room, Member hostMember) {
        List<Member> friendMembers = request.friendPhoneNumbers().stream()
                .map(phoneNumber -> memberRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new BadRequestException("전화번호 [" + phoneNumber + "] 에 해당하는 친구를 찾을 수 없습니다.")))
                .collect(Collectors.toList());

        List<Member> allMembersForValidation = new ArrayList<>(friendMembers);
        allMembersForValidation.add(hostMember); // 호스트도 전체 유효성 검사 목록에 포함

        // 3. 유효성 검증
        // 3.1. 모든 유저의 티켓 수를 확인
        validateSufficientTicketsForFriends(allMembersForValidation);

        // 3.2 해당 전화번호를 통해 조회한 유저가 DB에 존재하는지
        for (Member member : allMembersForValidation) {
            if (!memberRepository.existsByPhoneNumber(member.getPhoneNumber())) {
                throw new BadRequestException("전화번호 [" + member.getPhoneNumber() + "] 에 해당하는 사용자를 찾을 수 없습니다.");
            }
        }

        // 3.3. 호스트, 친구들 사이에 전화번호가 중복되지 않는지
        validatePhoneNumberUniqueness(allMembersForValidation);

        // 3.4. 성별이 호스트의 성별과 일치하는지
        validateFriendGroupGender(friendMembers, hostMember.getGender());


        // 4. 모든 유효성 검증이 끝났다면 방 생성 진행
        List<RoomParticipant> participants = new ArrayList<>();
        for (Member friend : friendMembers) {
            participants.add(RoomParticipant.createParticipant(room, friend, ParticipantRole.HOST));
        }
        participants.add(RoomParticipant.createHost(room, hostMember));

        return participants;
    }

    private void validateSufficientTicketsForFriends(List<Member> members) {
        for (Member friend : members) {
            if (friend.getRemainingTicket() <= 0) {
                throw new BadRequestException("친구 " + friend.getNickname() + "님의 티켓이 부족합니다.");
            }
        }
    }

    private void validatePhoneNumberUniqueness(List<Member> members) {
        long distinctPhoneNumbers = members.stream()
                .map(Member::getPhoneNumber)
                .distinct()
                .count();
        if (distinctPhoneNumbers < members.size()) {
            throw new BadRequestException(PHONE_NUMBER_DUPLICATE_AMONG_PARTICIPANTS);
        }
    }

    private void validateFriendGroupGender(List<Member> friendMembers, Gender hostGender) {
        for (Member friend : friendMembers) {
            if (friend.getGender() != hostGender) {
                throw new BadRequestException("친구 " + friend.getNickname() + "님의 성별(" + friend.getGender() + ")이 호스트님의 성별(" + hostGender + ")과 일치하지 않습니다.");
            }
        }
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
