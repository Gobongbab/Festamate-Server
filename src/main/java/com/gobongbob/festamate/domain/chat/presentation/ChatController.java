package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @Override
    @MessageMapping("/chat/room/{roomId}")
    public SuccessResponse<Void> sendMessage(
            @DestinationVariable("roomId") Long roomId,
            Authentication authentication,
            MessageRequest request
    ) {
        Member member = ((CustomMemberDetails) authentication.getPrincipal()).getMember();
        chatService.sendMessage(roomId, member, request.message());

        return new SuccessResponse<>();
    }

    @Override
    @GetMapping("/api/messages/room/{roomId}")
    public SuccessResponse<Slice<MessageResponse>> findMessages(
            @AuthenticationPrincipal Member member,
            @PathVariable("roomId") Long roomId,
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<MessageResponse> messages = chatService.findMessagesByRoomId(member.getId(), roomId, pageable);

        return new SuccessResponse<>(messages);
    }
}