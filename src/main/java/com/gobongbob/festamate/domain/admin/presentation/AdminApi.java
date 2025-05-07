package com.gobongbob.festamate.domain.admin.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.LoginRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.AuthResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Admin", description = "관리자 관련 API")
public interface AdminApi {

    @Operation(summary = "관리자용 회원 조회", description = "관리자가 회원 ID로 회원 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/users/{userId}")
    SuccessResponse<MemberResponse> getUserById(
            @Parameter(description = "인증된 관리자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "userId", description = "조회할 회원 ID")
            @PathVariable("userId") Long userId
    );

    @Operation(summary = "관리자용 모임방 삭제", description = "관리자 권한으로 모임방을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @DeleteMapping("/rooms/{roomId}")
    SuccessResponse<Void> deleteRoomById(
            @Parameter(description = "인증된 관리자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "삭제할 모임방 ID")
            @PathVariable("roomId") Long roomId
    );

    @Operation(summary = "관리자용 회원 제재", description = "회원을 제재 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PostMapping("/users/{userId}/block")
    SuccessResponse<Void> blockUser(
            @Parameter(description = "인증된 관리자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "userId", description = "제재할 회원 ID")
            @PathVariable("userId") Long userId
    );

    @Operation(summary = "관리자용 회원 제재 해제", description = "회원의 제재 상태를 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PostMapping("/users/{userId}/unblock")
    SuccessResponse<Void> unblockUser(
            @Parameter(description = "인증된 관리자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "userId", description = "제재 해제할 회원 ID")
            @PathVariable("userId") Long userId
    );

    @Operation(summary = "관리자용 일반 로그인", description = "관리자 아이디와 비밀번호로 로그인하여 JWT 토큰(액세스 토큰)을 발급받고, 리프레시 토큰은 쿠키에 설정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class,
                            oneOf = {AuthResponse.class}
                    ))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패 (사용자 없음 또는 비밀번호 불일치)"),
            @ApiResponse(responseCode = "403", description = "접근 거부 (관리자 권한 없음)")
    })
    @PostMapping("/login")
    ResponseEntity<SuccessResponse<AuthResponse>> loginAdmin(
            @RequestBody(description = "로그인 요청 정보 (아이디, 비밀번호)", required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class)))
            LoginRequest loginRequest,
            HttpServletResponse response
    );

    @Operation(summary = "관리자용 모든 신고 조회", description = "모든 신고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/report")
    SuccessResponse<List<ReportRoomResponse>> getAllReports(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    );

    @Operation(summary = "관리자용 미처리 신고 조회", description = "미처리된 신고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/report/unprocessed")
    SuccessResponse<List<ReportRoomResponse>> getUnprocessedReports(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    );
}
