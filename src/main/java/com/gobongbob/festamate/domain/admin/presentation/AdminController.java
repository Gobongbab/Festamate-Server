package com.gobongbob.festamate.domain.admin.presentation;

import com.gobongbob.festamate.domain.admin.application.AdminService;
import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.LoginRequest;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.room.application.RoomService;
import com.gobongbob.festamate.global.response.SuccessResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController implements AdminApi {

    private final MemberService memberService;
    private final RoomService roomService;
    private final AdminService adminService;

    @Override
    @GetMapping("/users/{userId}")
    public SuccessResponse<MemberResponse> getUserById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("userId") Long userId
    ) {
        return new SuccessResponse<>(memberService.findMemberByIdForAdmin(memberDetails, userId));
    }

    @Override
    @DeleteMapping("/rooms/{roomId}")
    public SuccessResponse<Void> deleteRoomById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        roomService.deleteRoomById(memberDetails.getMember(), roomId);
        return new SuccessResponse<>();
    }

    @Override
    @PostMapping("/users/{userId}/block")
    public SuccessResponse<Void> blockUser(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("userId") Long userId
    ) {
        memberService.blockMemberById(userId);
        return new SuccessResponse<>();
    }

    @Override
    @PostMapping("/users/{userId}/unblock")
    public SuccessResponse<Void> unblockUser(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("userId") Long userId
    ) {
        memberService.unblockMemberById(userId);
        return new SuccessResponse<>();
    }

    // 관리자 로그인
    @Override
    @PostMapping("/login")
    public SuccessResponse<Map<String, String>> loginAdmin(
            @RequestBody
            LoginRequest loginRequest
    ) {
        Map<String, String> tokens = adminService.loginAdmin(loginRequest);
        return new SuccessResponse<>(tokens);
    }
}
