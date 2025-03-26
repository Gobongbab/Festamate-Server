package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.application.MessageService;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @PathVariable Long roomId,
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<MessageResponse> messages = messageService.findMessagesByRoomId(member.getId(), roomId, pageable);

        return ResponseEntity.ok(messages);
    }
}
