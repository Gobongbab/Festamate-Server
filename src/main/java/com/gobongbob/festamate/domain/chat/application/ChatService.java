package com.gobongbob.festamate.domain.chat.application;

import static com.gobongbob.festamate.global.response.ResponseCode.CHAT_ROOM_NOT_FOUND;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_AUTHORITY_CHAT_ROOM;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.domain.Message;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.persistence.RoomParticipantRepository;
import com.gobongbob.festamate.global.NotificationService;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final NotificationService notificationService;

    @Transactional
    public void sendMessage(Long chatRoomId, Member member, String message) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BadRequestException(CHAT_ROOM_NOT_FOUND));

        Message savedMessage = messageRepository.save(
                Message.builder()
                        .chatRoom(chatRoom)
                        .sender(member)
                        .message(message)
                        .sendDate(LocalDateTime.now())
                        .build()
        );
        MessageResponse response = MessageResponse.fromEntity(savedMessage);

        // 메시지를 보낸 방에 참여 중인 모든 유저 조회
        List<RoomParticipant> participants = roomParticipantRepository.findByRoom_Id(chatRoom.getRoom().getId());

        messagingTemplate.convertAndSend("/topic/chatRooms/" + chatRoomId, response);

        participants.forEach(participant -> {
            String fcmToken = participant.getMember().getFcmToken(); // FCM 토큰 가져오기
            Long participantId = participant.getMember().getId();
            if (fcmToken != null) {
                notificationService.sendNotification(
                        fcmToken,
                        "채팅 메시지가 도착했습니다",
                        member.getName() + ":" + message,
                        participantId
                );
            }
        });
    }

    // 메시지 조회
    @CheckActiveUser
    public Slice<MessageResponse> findMessagesByRoomId(Long memberId, Long roomId,
            Pageable pageable) {
//        validateRoomParticipation(memberId, roomId);

        return messageRepository.findByRoomId(roomId, pageable)
                .map(MessageResponse::fromEntity);
    }

    private void validateRoomParticipation(Long memberId, Long roomId) {
        if (roomParticipantRepository.findByRoom_IdAndMember_Id(memberId, roomId).isEmpty()) {
            throw new BadRequestException(NO_AUTHORITY_CHAT_ROOM);
        }
    }
}
