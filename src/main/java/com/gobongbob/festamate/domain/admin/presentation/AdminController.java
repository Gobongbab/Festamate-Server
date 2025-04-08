package com.gobongbob.festamate.domain.admin.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.room.application.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final MemberService memberService;
    private final RoomService roomService;

    // 유저 조회(admin)
    @GetMapping("/users/{userId}")
    public ResponseEntity<MemberResponse> getUserById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(memberService.findMemberByIdForAdmin(memberDetails, userId));
    }

    // 방 삭제(admin)
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoomById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId
    ) {
        roomService.deleteRoomById(memberDetails.getMember(), roomId);
        return ResponseEntity.ok().build();
    }

    // 유저 제재(admin)
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long userId
    ) {
        memberService.blockMemberById(userId);
        return ResponseEntity.ok().build();
    }

    // 유저 제재 해제(admin)
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long userId
    ) {
        memberService.unblockMemberById(userId);
        return ResponseEntity.ok().build();
    }
}
