package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.chat.dto.request.ChatRequest;
import com.gobongbob.festamate.domain.chat.dto.response.ChatResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/room/{roomId}") // Spring App 을 거쳐서 메시지 전송. 앞에 "app" prefix 를 붙여야 함
    public ResponseEntity<ChatResponse> chat(
            @DestinationVariable Long roomId,
            @AuthenticationPrincipal Member member,
            ChatRequest request
    ) {
        return ResponseEntity.ok(chatService.createChat(roomId, member, request));
    }
}
