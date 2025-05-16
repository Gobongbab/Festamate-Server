package com.gobongbob.festamate.domain.room.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ERROR_SEND_SMS;
import static com.gobongbob.festamate.global.response.ResponseCode.FAIL_SEND_SMS;
import static com.gobongbob.festamate.global.response.ResponseCode.FRIEND_GENDER_NOT_MATCH_WITH_HOST;
import static com.gobongbob.festamate.global.response.ResponseCode.MEMBER_NOT_FOUND_BY_PHONE_NUMBER;
import static com.gobongbob.festamate.global.response.ResponseCode.MUST_HOST;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_ENOUGH_TICKET;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;
import static com.gobongbob.festamate.global.response.ResponseCode.PHONE_NUMBER_DUPLICATE_AMONG_PARTICIPANTS;
import static com.gobongbob.festamate.global.response.ResponseCode.ROOM_UPDATE_NOT_AVAILABLE;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.domain.Member.MemberStatus;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.domain.ParticipantRole;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomAuthority;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.domain.Status;
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
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;
    private final MemberRepository memberRepository;
    private final DefaultMessageService messageService;

    @Value("${coolsms.from.number}")
    private String fromNumber;

    // 방 생성
    @Transactional
    @CheckActiveUser// 메서드 실행 전 현재 사용자가 ACTIVE 상태인지 AOP로 확인 (BLOCKED 시 AccessDeniedException 발생)
    public Room createRoom(Long memberId, RoomCreateRequest request, List<MultipartFile> imageFiles) {
        Member hostMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

        Room createdRoom = roomRepository.save(request.toEntity(hostMember));
        uploadImageIfExist(imageFiles, createdRoom);

//        ChatRoom chatRoom = ChatRoom.createChatRoom(createdRoom.getTitle(), createdRoom);
//        chatRoomRepository.save(chatRoom);

        List<RoomParticipant> participants = collectAndValidateInitialParticipants(
                request.friendPhoneNumbers(),
                createdRoom,
                hostMember
        );

        participants.forEach(participant -> {
            roomParticipantRepository.save(participant);
            participant.getMember().useTicket(); // 각 참가자의 티켓 사용
        });
        sendMatchingCompleteMessages(hostMember, participants, createdRoom);

        return createdRoom;
    }

    private List<RoomParticipant> collectAndValidateInitialParticipants(FriendPhoneNumbersRequest request, Room room,
            Member hostMember) {
        List<Member> friendMembers = request.friendPhoneNumbers().stream()
                .map(phoneNumber -> memberRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new BadRequestException(
                                MEMBER_NOT_FOUND_BY_PHONE_NUMBER.formatMessage(phoneNumber)))
                ).collect(Collectors.toList());

        List<Member> allMembersForValidation = new ArrayList<>(friendMembers);
        allMembersForValidation.add(hostMember); // 호스트도 전체 유효성 검사 목록에 포함

        // 3. 유효성 검증
        // 3.1. 모든 유저의 티켓 수를 확인
        validateSufficientTicketsForFriends(allMembersForValidation);

        // 3.2 해당 전화번호를 통해 조회한 유저가 DB에 존재하는지
        for (Member member : allMembersForValidation) {
            if (!memberRepository.existsByPhoneNumber(member.getPhoneNumber())) {
                throw new BadRequestException(MEMBER_NOT_FOUND_BY_PHONE_NUMBER.formatMessage(member.getPhoneNumber()));
            }
        }

        // 3.3. 호스트, 친구들 사이에 전화번호가 중복되지 않는지
        validatePhoneNumberUniqueness(allMembersForValidation);

        // 3.4 참여자들 중 제재된 회원이 없는지
        validateParticipantsActive(allMembersForValidation);

        // 3.5. 성별이 호스트의 성별과 일치하는지
        validateFriendGroupGender(friendMembers, hostMember);

        // 4. 모든 유효성 검증이 끝났다면 방 생성 진행
        List<RoomParticipant> participants = new ArrayList<>();
        for (Member friend : friendMembers) {
            participants.add(RoomParticipant.createParticipant(room, friend, ParticipantRole.HOST));
        }
        participants.add(RoomParticipant.createHost(room, hostMember));

        return participants;
    }

    private void sendMatchingCompleteMessages(Member host, List<RoomParticipant> participants, Room room) {
        participants.stream()
                .map(RoomParticipant::getMember)
                .filter(member -> !member.equals(host))
                .map(Member::getPhoneNumber)
                .forEach(phoneNumber -> {
                    Message message = setMessage(host, phoneNumber, room.getTitle());
                    try {
                        messageService.send(message);
                    } catch (NurigoMessageNotReceivedException e) {
                        log.error("(NurigoMessageNotReceivedException) 휴대폰 문자 전송 에러 상세 내용: " + e);
                        throw new BadRequestException(FAIL_SEND_SMS);
                    } catch (Exception e) {
                        log.error("(Exception) 휴대폰 문자 전송 에러 상세 내용: " + e);
                        throw new BadRequestException(ERROR_SEND_SMS);
                    }
                });
    }

    private Message setMessage(Member host, String phoneNumber, String title) {
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(phoneNumber);
        message.setText("[FestaMate!] "
                + host.getNickname() + "님의 모임방 "
                + title + "에 초대됐어요! 매칭이 완료되면 오픈채팅 링크를 보내드릴게요.");

        return message;
    }

    private void validateSufficientTicketsForFriends(List<Member> members) {
        for (Member friend : members) {
            if (friend.getRemainingTicket() <= 0) {
                throw new BadRequestException(NOT_ENOUGH_TICKET.formatMessage(friend.getNickname()));
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

    private void validateParticipantsActive(List<Member> participants) {
        boolean hasBlockedParticipant = participants.stream()
                .anyMatch(member -> member.getStatus() == MemberStatus.BLOCKED);

        if (hasBlockedParticipant) {
            throw new BadRequestException(NO_MEMBER);
        }
    }

    private void validateFriendGroupGender(List<Member> friendMembers, Member host) {
        for (Member friend : friendMembers) {
            if (friend.getGender() != host.getGender()) {
                throw new BadRequestException(FRIEND_GENDER_NOT_MATCH_WITH_HOST.formatMessage(
                        friend.getNickname(),
                        friend.getGender().getName(),
                        host.getNickname(),
                        host.getGender().getName()
                ));
            }
        }
    }

    // 방 전체 조회
    public Slice<RoomListResponse> findBySearchCondition(Pageable pageable, FilteringCondition filteringCondition) {
        return roomRepository.findBySearchCondition(pageable, filteringCondition)
                .map(RoomListResponse::fromEntity);
    }

    // 추천 모임방 조회
    public Slice<RoomListResponse> findRecommendedRooms(Pageable pageable, Member member) {
        return roomRepository.findRecommendedRooms(pageable, member)
                .map(RoomListResponse::fromEntity);
    }

    // 참여 중인 모임방 조회
    @CheckActiveUser
    public Slice<RoomListResponse> findParticipatingRooms(Long memberId, Pageable pageable) {
        return roomParticipantRepository.findByMember_Id(memberId, pageable)
                .map(RoomParticipant::getRoom)
                .map(RoomListResponse::fromEntity);
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
        validateIsAccessible(room, member);
        validateIsMatchingRoom(room);

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
                Integer.parseInt(request.preferredStudentIdMin()),
                Integer.parseInt(request.preferredStudentIdMax()),
                request.meetingDateTime(),
                request.maxParticipants()
        );
//        room.getChatRoom().updateTitle(request.title());
    }

    // 방 삭제(일반, admin)
    @Transactional
    @CheckActiveUser
    public void deleteRoomById(Member member, Long roomId) {
        Room room = roomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));
        validateIsAccessible(room, member);
        validateIsMatchingRoom(room);

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
            Image image = setBasicImage();
            RoomImage roomImage = RoomImage.fromEntity(image);
            createdRoom.assignImages(List.of(roomImage));
        }
    }

    private Image setBasicImage() {
        String basicImageUrl = "https://festamate-bucket.s3.ap-northeast-2.amazonaws.com/femalogo.png";
        UUID uuid = UUID.randomUUID();

        return Image.builder()
                .url(basicImageUrl)
                .uploadName("페메 로고")
                .storeName("페메 로고" + uuid)
                .build();
    }


    private void validateIsAccessible(Room room, Member member) {
        if (!member.isHost(room) && !member.isAdmin()) {
            throw new BadRequestException(MUST_HOST);
        }
    }

    private void validateIsMatchingRoom(Room room) {
        if (room.getStatus() != Status.MATCHING) {
            throw new BadRequestException(ROOM_UPDATE_NOT_AVAILABLE);
        }
    }
}
