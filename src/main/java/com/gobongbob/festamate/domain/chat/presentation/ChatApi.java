package com.gobongbob.festamate.domain.chat.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.dto.request.MessageRequest;
import com.gobongbob.festamate.domain.chat.dto.response.MessageResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Chat", description = "채팅 관련 API")
public interface ChatApi {

    // @MessageMapping의 경우, STOMP와 관련된 프로토콜이기 때문에 문서화가 되지 않아 별도의 API 문서를 제작해야 합니다.
    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @MessageMapping("/chat/room/{roomId}")
    SuccessResponse<Void> sendMessage(
            @Parameter(name = "roomId", description = "모임방 ID")
            @DestinationVariable("roomId") Long roomId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication,
            @Parameter(description = "메시지 전송 요청 정보")
            MessageRequest request
    );

    // 메시지 조회
    @Operation(summary = "메시지 조회", description = "모임방의 메시지 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @GetMapping("/api/messages/chatRooms/{chatRoomId}")
    SuccessResponse<Slice<MessageResponse>> findMessages(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "모임방 ID")
            @PathVariable("roomId") Long roomId,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    );
}
