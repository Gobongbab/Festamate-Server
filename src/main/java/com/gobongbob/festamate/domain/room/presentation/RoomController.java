package com.gobongbob.festamate.domain.room.presentation;

import com.gobongbob.festamate.domain.room.application.RoomParticipationService;
import com.gobongbob.festamate.domain.room.application.RoomService;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @AuthenticationPrincipal Member member,
            @RequestBody RoomCreateRequest request
    ) {
        roomService.createRoom(member, request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<RoomResponse>> findAllRooms() {
        return ResponseEntity.ok(roomService.findAllRooms());
    }

    @GetMapping("/participate")
    public ResponseEntity<RoomResponse> findParticipatingRooms(Long memberId) {
        return ResponseEntity.ok(roomService.findParticipatingRooms(memberId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> findRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.findRoomById(roomId));
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> updateRoomById(
            @PathVariable Long roomId,
            @RequestBody RoomUpdateRequest request,
            Long memberId
    ) {
        roomService.updateRoomById(roomId, memberId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long roomId, Long memberId) {
        roomService.deleteRoomById(roomId, memberId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/participate")
    public ResponseEntity<Void> participateRoom(@PathVariable Long roomId, Long memberId) {
        roomParticipationService.participateRoom(roomId, memberId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId, Long memberId) {
        roomParticipationService.leaveRoomById(roomId, memberId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/isHost")
    public ResponseEntity<Boolean> isMemberHost(@PathVariable Long roomId, Long memberId) {
        return ResponseEntity.ok(roomParticipationService.isMemberHost(roomId, memberId));
    }
}
