package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.chat.dto.request.ChatRequest;
import com.gobongbob.festamate.domain.chat.dto.response.ChatResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/room/{roomId}") // 이거 쓸라면 /publish/chat/{roomId} 엔드포인트 필요
    @SendTo("/subscribe/room/{roomId}")   //구독하고 있는 장소로 메시지 전송 (목적지)  -> WebSocketConfig Broker 에서 적용한건 앞에 붙어줘야됨
    public ResponseEntity<ChatResponse> chat(
            @DestinationVariable Long roomId,
            @AuthenticationPrincipal Member member,
            ChatRequest request
    ) {
        return ResponseEntity.ok(chatService.createChat(roomId, member, request));
    }
}
