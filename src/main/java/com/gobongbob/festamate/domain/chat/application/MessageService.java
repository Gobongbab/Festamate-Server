package com.gobongbob.festamate.domain.chat.application;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.chat.domain.Message;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.chat.persistence.ChatRoomRepository;
import com.gobongbob.festamate.domain.chat.persistence.MessageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public MessageResponse send(Long roomId, Member member, MessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        Message message = Message.builder()
                .room(chatRoom)
                .nickname(member.getNickname())
                .message(request.message())
                .build();
        Message savedMessage = messageRepository.save(message);

        return MessageResponse.fromEntity(savedMessage);
    }

    public List<MessageResponse> findMessagesByRoomId(Long roomId) {
        return messageRepository.findByRoomId(roomId)
                .stream()
                .map(MessageResponse::fromEntity)
                .toList();
    }
}
