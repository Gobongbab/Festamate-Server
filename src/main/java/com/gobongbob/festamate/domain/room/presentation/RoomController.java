package com.gobongbob.festamate.domain.room.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.room.application.RoomParticipationService;
import com.gobongbob.festamate.domain.room.application.RoomService;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomParticipationService roomParticipationService;

    @PostMapping("")
    public ResponseEntity<Void> createRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody RoomCreateRequest request
    ) {
        roomService.createRoom(memberDetails.getMember(), request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<RoomResponse>> findAllRooms() {
        return ResponseEntity.ok(roomService.findAllRooms());
    }

    @GetMapping("/participate")
    public ResponseEntity<RoomResponse> findParticipatingRooms(
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
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

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId
    ) {
        roomParticipationService.leaveRoomById(memberDetails.getMember(), roomId);

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
