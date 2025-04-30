package com.gobongbob.festamate.domain.chat.application;

import static com.gobongbob.festamate.global.response.ResponseCode.CHAT_ROOM_NOT_FOUND;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_AUTHORITY_CHAT_ROOM;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.domain.Message;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.persistence.RoomParticipantRepository;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.time.LocalDateTime;
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

        log.info("[ChatService]");
        log.info("Chat Room ID: " + chatRoomId);
        log.info("Sender ID: " + member.getId());
        log.info("Message: " + message);
        log.info("Response: " + response);
        log.info("Saved Message ID: " + savedMessage.getId());
        log.info("Saved Message Sender ID: " + savedMessage.getSender().getId());
        log.info("Saved Message Content: " + savedMessage.getMessage());
        log.info("Saved Message Send Date: " + savedMessage.getSendDate());
        log.info("Saved Message Chat Room ID: " + savedMessage.getChatRoom().getId());
        log.info("Response Sender Nickname: " + response.nickname());
        log.info("Response Sender Message: " + response.message());

        messagingTemplate.convertAndSend("/topic/chatRooms/" + chatRoomId, response);
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
