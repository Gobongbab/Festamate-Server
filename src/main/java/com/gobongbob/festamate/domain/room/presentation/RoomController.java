package com.gobongbob.festamate.domain.room.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.room.application.RoomParticipationService;
import com.gobongbob.festamate.domain.room.application.RoomService;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import com.gobongbob.festamate.domain.room.dto.request.FriendPhoneNumbersRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.IsMemberHostResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController implements RoomApi {

    private final RoomService roomService;
    private final RoomParticipationService roomParticipationService;
    private final ChatService chatService;

    @Override
    @PostMapping("")
    public SuccessResponse<Void> create(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestPart("request") @Valid RoomCreateRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> multipartFiles
    ) {
        ChatRoom createdChatRoom = roomService.createRoom(memberDetails.getMember().getId(), request, multipartFiles);
        chatService.sendMessage(
                createdChatRoom.getId(),
                memberDetails.getMember(),
                "안녕하세요! " + createdChatRoom.getName() + "에 오신 것을 환영합니다!"
        );

        return new SuccessResponse<>();
    }

    @Override
    @GetMapping("")
    public SuccessResponse<Slice<RoomListResponse>> findBySearchCondition(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute FilteringCondition filteringCondition
    ) {
        return new SuccessResponse<>(roomService.findBySearchCondition(pageable, filteringCondition));
    }

    @Override
    @GetMapping("/participations")
    public SuccessResponse<List<RoomListResponse>> findParticipatingRooms(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(roomService.findParticipatingRooms(memberDetails.getMember().getId()));
    }

    @Override
    @GetMapping("/{roomId}")
    public SuccessResponse<RoomResponse> findRoomById(@PathVariable("roomId") Long roomId) {
        return new SuccessResponse<>(roomService.findRoomById(roomId));
    }

    @Override
    @PatchMapping("/{roomId}")
    public SuccessResponse<Void> updateById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId,
            @RequestBody @Valid RoomUpdateRequest request
    ) {
        roomService.updateRoomById(memberDetails.getMember(), roomId, request);

        return new SuccessResponse<>();
    }

    @Override
    @DeleteMapping("/{roomId}")
    public SuccessResponse<Void> deleteById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        roomService.deleteRoomById(memberDetails.getMember(), roomId);

        return new SuccessResponse<>();
    }

    @Override
    @PostMapping("/{roomId}/participations")
    public SuccessResponse<Void> participate(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId,
            @RequestBody FriendPhoneNumbersRequest request
    ) {
        ChatRoom chatRoom = roomParticipationService.participate(memberDetails.getMember().getId(), roomId, request);
        chatService.sendMessage(
                chatRoom.getId(),
                memberDetails.getMember(),
                memberDetails.getMember().getNickname() + "님이 들어왔습니다."
        );

        return new SuccessResponse<>();
    }

    @Override
    @PostMapping("/{roomId}/leave")
    public SuccessResponse<Void> leave(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        roomParticipationService.leave(memberDetails.getMember(), roomId);
        chatService.sendMessage(
                roomId,
                memberDetails.getMember(),
                memberDetails.getMember().getNickname() + "님이 나갔습니다."
        );

        return new SuccessResponse<>();
    }

    @Override
    @GetMapping("/{roomId}/host")
    public SuccessResponse<IsMemberHostResponse> isMemberHost(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(roomParticipationService.isMemberHost(roomId, memberDetails.getMember()));
    }
}
