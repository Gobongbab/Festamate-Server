package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @Override
    @MessageMapping("/messages/chatRooms/{chatRoomId}")
    public SuccessResponse<Void> sendMessage(
            @DestinationVariable("chatRoomId") Long chatRoomId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Payload MessageRequest request
    ) {
        log.debug("memberDetails: " + memberDetails);
        log.debug("memberDetails.getMember(): " + memberDetails.getMember());
        Member member = memberDetails.getMember();
        log.debug("[ChatController]");
        log.debug("Chat Room ID: " + chatRoomId);
        log.debug("Request: " + request);
        log.debug("Message: " + request.message());

        chatService.sendMessage(chatRoomId, member, request.message());

        return new SuccessResponse<>();
    }

    // 메시지 조회
    @Override
    @GetMapping("/api/messages/chatRooms/{chatRoomId}")
    public SuccessResponse<Slice<MessageResponse>> findMessages(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("chatRoomId") Long chatRoomId,
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<MessageResponse> messages = chatService.findMessagesByRoomId(
                memberDetails.getMember().getId(),
                chatRoomId,
                pageable
        );

        return new SuccessResponse<>(messages);
    }
}