package com.gobongbob.festamate.domain.chat.application;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.domain.Message;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.presentation.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RoomParticipantRepository roomParticipantRepository;

    @Transactional
    public MessageResponse createMessage(Long roomId, Member member, MessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        Message message = Message.builder()
                .charRoom(chatRoom)
                .sender(member)
                .message(request.message())
                .build();
        Message savedMessage = messageRepository.save(message);

        return MessageResponse.fromEntity(savedMessage);
    }

    public Slice<MessageResponse> findMessagesByRoomId(Long memberId, Long roomId, Pageable pageable) {
        validateRoomParticipation(memberId, roomId);

        return messageRepository.findByRoomId(roomId, pageable)
                .map(MessageResponse::fromEntity);
    }

    private void validateRoomParticipation(Long memberId, Long roomId) {
        if (roomParticipantRepository.findByRoom_IdAndMember_Id(memberId, roomId).isEmpty()) {
            throw new IllegalArgumentException("채팅방을 조회할 수 있는 권한이 없습니다.");
        }
    }
}
