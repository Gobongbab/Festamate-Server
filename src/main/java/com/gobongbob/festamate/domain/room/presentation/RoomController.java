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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController implements RoomApi {

    private final RoomService roomService;
    private final RoomParticipationService roomParticipationService;
    private final ChatService chatService;

    // 방 생성
    @Override
    @PostMapping("")
    public SuccessResponse<Void> create(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestPart("request") @Valid RoomCreateRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> multipartFiles
    ) {
        ChatRoom createdChatRoom = roomService.createRoom(memberDetails.getMember().getId(),
                request, multipartFiles);
        chatService.sendMessage(
                createdChatRoom.getId(),
                memberDetails.getMember(),
                "안녕하세요! " + createdChatRoom.getTitle() + "에 오신 것을 환영합니다!"
        );

        return new SuccessResponse<>();
    }

    // 방 전체 조회
    @Override
    @GetMapping("")
    public SuccessResponse<Slice<RoomListResponse>> findBySearchCondition(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute FilteringCondition filteringCondition
    ) {
        return new SuccessResponse<>(roomService.findBySearchCondition(pageable, filteringCondition));
    }

    // 추천 모임방 조회
    @Override
    @GetMapping("/recommended")
    public SuccessResponse<Slice<RoomListResponse>> findRecommendedRooms(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new SuccessResponse<>(roomService.findRecommendedRooms(pageable, memberDetails.getMember()));
    }

    // 참여 중인 모임방 조회
    @Override
    @GetMapping("/participations")
    public SuccessResponse<Slice<RoomListResponse>> findParticipatingRooms(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new SuccessResponse<>(roomService.findParticipatingRooms(memberDetails.getMember().getId(), pageable));
    }

    @Override
    @GetMapping("/{roomId}")
    public SuccessResponse<RoomResponse> findRoomById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        return new SuccessResponse<>(roomService.findRoomById(memberDetails, roomId));
    }

    // 모임방 정보 수정
    @Override
    @PatchMapping("/{roomId}")
    public SuccessResponse<Void> updateById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId,
            @RequestPart("request") @Valid RoomUpdateRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> multipartFiles
    ) {
        roomService.updateRoomById(memberDetails.getMember(), roomId, request, multipartFiles);

        return new SuccessResponse<>();
    }

    // 모임방 삭제
    @Override
    @DeleteMapping("/{roomId}")
    public SuccessResponse<Void> deleteById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        roomService.deleteRoomById(memberDetails.getMember(), roomId);

        return new SuccessResponse<>();
    }

    // 방 참여
    @Override
    @Transactional
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

    // 모임방 나가기
    @Override
    @PostMapping("/{roomId}/leave")
    public SuccessResponse<Void> leave(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        ChatRoom chatRoom = roomParticipationService.leave(memberDetails.getMember(), roomId);
        chatService.sendMessage(
                chatRoom.getId(),
                memberDetails.getMember(),
                memberDetails.getMember().getNickname() + "님이 나갔습니다."
        );

        return new SuccessResponse<>();
    }

    // 방장 여부 확인
    @Override
    @GetMapping("/{roomId}/host")
    public SuccessResponse<IsMemberHostResponse> isMemberHost(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(roomParticipationService.isMemberHost(roomId, memberDetails.getMember()));
    }
}
