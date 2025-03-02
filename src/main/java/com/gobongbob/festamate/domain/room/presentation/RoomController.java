package com.gobongbob.festamate.domain.room.presentation;

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

    @PostMapping("")
    public ResponseEntity<Void> createRoom(
            @RequestBody RoomCreateRequest request,
            Long userId // 추후 Spring Security를 활용하여 사용자 정보를 가져오도록 변경 필요 (변경 후 주석 삭제)
    ) {
        roomService.createRoom(request, userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<RoomResponse>> findAllRooms() {
        return ResponseEntity.ok(roomService.findAllRooms());
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> findRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.findRoomById(roomId));
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> updateRoomById(
            @PathVariable Long roomId,
            @RequestBody RoomUpdateRequest request
    ) {
        roomService.updateRoomById(roomId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long roomId) {
        roomService.deleteRoomById(roomId);

        return ResponseEntity.ok().build();
    }
}
