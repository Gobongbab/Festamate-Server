package com.gobongbob.festamate.domain.room.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.chat.application.ChatService;
import com.gobongbob.festamate.domain.room.application.RoomParticipationService;
import com.gobongbob.festamate.domain.room.application.RoomService;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomParticipationService roomParticipationService;
    private final ChatService chatService;

    @PostMapping("")
    public ResponseEntity<Void> createRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestPart("request") RoomCreateRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> multipartFiles
    ) {
        Room createdRoom = roomService.createRoom(memberDetails.getMember(), request, multipartFiles);
        chatService.sendMessage(
                createdRoom.getId(),
                memberDetails.getMember(),
                "안녕하세요! " + createdRoom.getTitle() + "에 오신 것을 환영합니다!"
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<RoomListResponse>> findAllRooms() {
        return ResponseEntity.ok(roomService.findAllRooms());
    }

    @GetMapping("/participate")
    public ResponseEntity<RoomResponse> findParticipatingRooms(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return ResponseEntity.ok(roomService.findParticipatingRooms(memberDetails.getMember().getId()));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> findRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.findRoomById(roomId));
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> updateRoomById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId,
            @RequestBody RoomUpdateRequest request
    ) {
        roomService.updateRoomById(memberDetails.getMember(), roomId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId
    ) {
        roomService.deleteRoomById(memberDetails.getMember(), roomId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/participate")
    public ResponseEntity<Void> participateRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId
    ) {
        roomParticipationService.participateRoom(memberDetails.getMember(), roomId);
        chatService.sendMessage(
                roomId,
                memberDetails.getMember(),
                memberDetails.getMember().getNickname() + "님이 들어왔습니다."
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId
    ) {
        roomParticipationService.leaveRoomById(memberDetails.getMember(), roomId);
        chatService.sendMessage(
                roomId,
                memberDetails.getMember(),
                memberDetails.getMember().getNickname() + "님이 나갔습니다."
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/isHost")
    public ResponseEntity<Boolean> isMemberHost(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(roomParticipationService.isMemberHost(memberDetails.getMember().getId(), roomId));
    }
}
