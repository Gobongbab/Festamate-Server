package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/messages")
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @Override
    @MessageMapping("/chatRooms/{chatRoomId}")
    public SuccessResponse<Void> sendMessage(
            @DestinationVariable("chatRoomId") Long chatRoomId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Payload MessageRequest request
    ) {
        chatService.sendMessage(chatRoomId, memberDetails.getMember(), request.message());

        return new SuccessResponse<>();
    }

//    @Override
//    @GetMapping("/chatRooms/participations")
//    public SuccessResponse<Slice<ChatRoomListResponse>> findParticipatingChatRooms(
//            @AuthenticationPrincipal CustomMemberDetails memberDetails,
//            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
//    ) {
//        return new SuccessResponse<>(chatService.findParticipatingChatRooms(pageable, memberDetails.getMember()));
//    }

    // 메시지 조회
    @Override
    @GetMapping("/chatRooms/{chatRoomId}")
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