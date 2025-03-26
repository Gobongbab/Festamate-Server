package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.chat.application.MessageService;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/room/{roomId}") // Spring App 을 거쳐서 메시지 전송. 앞에 "app" prefix 를 붙여야 함
    public ResponseEntity<Void> sendMessage(
            @DestinationVariable Long roomId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            MessageRequest request
    ) {
        MessageResponse messageResponse = messageService.createMessage(roomId, memberDetails.getMember(), request);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, messageResponse);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("api/room/{roomId}/messages")
    public ResponseEntity<Slice<MessageResponse>> findMessages(
            @AuthenticationPrincipal Member member,
            MessageRequest request
    ) {
        return ResponseEntity.ok(messageService.send(roomId, member, request));
    }
}
