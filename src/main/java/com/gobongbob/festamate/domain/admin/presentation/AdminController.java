package com.gobongbob.festamate.domain.admin.presentation;

import com.gobongbob.festamate.domain.admin.application.AdminService;
import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.LoginRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.report.application.ReportService;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import com.gobongbob.festamate.domain.room.application.RoomService;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController implements AdminApi {

    private final MemberService memberService;
    private final RoomService roomService;
    private final AdminService adminService;
    private final ReportService reportService;

    // 관리자용 유저 목록 조회
    @Override
    @GetMapping("/users/{userId}")
    public SuccessResponse<MemberResponse> getUserById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("userId") Long userId
    ) {
        return new SuccessResponse<>(memberService.findMemberByIdForAdmin(memberDetails, userId));
    }

    // 관리자용 사용자 이름 검색
    @GetMapping("/users/search")
    public SuccessResponse<List<MemberResponse>> searchUsersByName(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam("name") String name
    ) {
        List<MemberResponse> results = memberService.findMembersByNameForAdmin(memberDetails, name);
        return new SuccessResponse<>(results);
    }

    // 관리자용 모임방 삭제
    @Override
    @DeleteMapping("/rooms/{roomId}")
    public SuccessResponse<Void> deleteRoomById(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId
    ) {
        roomService.deleteRoomById(memberDetails.getMember(), roomId);
        return new SuccessResponse<>();
    }

    // 관리자용 유저 제재
    @Override
    @PostMapping("/users/{userId}/block")
    public SuccessResponse<Void> blockUser(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("userId") Long userId
    ) {
        memberService.blockMemberById(userId);
        return new SuccessResponse<>();
    }

    // 관리자용 유저 제재 해제
    @Override
    @PostMapping("/users/{userId}/unblock")
    public SuccessResponse<Void> unblockUser(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("userId") Long userId
    ) {
        memberService.unblockMemberById(userId);
        return new SuccessResponse<>();
    }

    // 관리자용 일반 로그인
    @Override
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> loginAdmin(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        // 1. AdminService 호출하여 토큰 Map 받기
        Map<String, String> tokens = adminService.loginAdmin(loginRequest);

        // 2. Map에서 accessToken과 refreshToken 추출
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // 3. 리프레시 토큰 HttpOnly 쿠키 설정 (아래 helper 메서드 사용)
        ResponseCookie refreshTokenCookie = createAdminRefreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 4. 본문에는 액세스 토큰만 포함하는 DTO 반환
        AuthResponse authResponse = new AuthResponse(accessToken); // accessToken만 담는 DTO
        return ResponseEntity.ok(new SuccessResponse<>(authResponse));
    }

    // 관리자용 RefreshToken 쿠키 생성 유틸리티 메서드
    private ResponseCookie createAdminRefreshTokenCookie(String refreshToken) {
        Duration adminRefreshTokenValidity = TokenType.ADMIN_REFRESH.getDuration(); // TokenType 사용 시

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)   // JavaScript 접근 불가
                .secure(true)     // HTTPS 환경에서만 전송
                .path("/")        // 쿠키 적용 경로 (애플리케이션 전체)
                .maxAge(adminRefreshTokenValidity) // 쿠키 만료 시간 (초 단위)
                .sameSite("None")
                .build();
    }

    // 관리자용 모든 신고 조회
    @Override
    @GetMapping("/report")
    public SuccessResponse<List<ReportRoomResponse>> getAllReports(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(reportService.getAllReports());
    }

    // 관리자용 미처리 신고 조회
    @Override
    @GetMapping("/report/unprocessed")
    public SuccessResponse<List<ReportRoomResponse>> getUnprocessedReports(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(reportService.getUnprocessedReports());
    }
}
