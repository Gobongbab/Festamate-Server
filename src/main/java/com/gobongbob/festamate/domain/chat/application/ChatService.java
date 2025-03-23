package com.gobongbob.festamate.domain.chat.application;

import com.gobongbob.festamate.domain.chat.domain.Chat;
import com.gobongbob.festamate.domain.chat.dto.request.ChatRequest;
import com.gobongbob.festamate.domain.chat.dto.response.ChatResponse;
import com.gobongbob.festamate.domain.chat.persistence.ChatRepository;
import com.gobongbob.festamate.domain.chatRoom.domain.ChatRoom;
import com.gobongbob.festamate.domain.chatRoom.repository.ChatRoomRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatResponse createChat(Long roomId, Member member, ChatRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        Chat chat = Chat.builder()
                .room(chatRoom)
                .nickname(member.getNickname())
                .message(request.message())
                .build();
        Chat savedChat = chatRepository.save(chat);

        return ChatResponse.fromEntity(savedChat);
    }

    public List<ChatResponse> getChatsByRoomId(Long roomId) {
        return chatRepository.findByRoomId(roomId)
                .stream()
                .map(ChatResponse::fromEntity)
                .toList();
    }
}
