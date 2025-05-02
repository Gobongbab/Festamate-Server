package com.gobongbob.festamate.domain.report.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.report.dto.request.ReportMemberRequest;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Report", description = "신고 관련 API")
public interface ReportApi {

    @Operation(summary = "모임방 신고", description = "모임방을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @PostMapping("/room/{roomId}")
    SuccessResponse<Void> reportRoom(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "신고할 모임방 ID")
            @PathVariable("roomId") Long roomId,
            @Parameter(description = "모임방 신고 요청 정보")
            @RequestBody @Valid ReportRoomRequest request
    );

    @Operation(summary = "사용자 신고", description = "사용자를 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "사용자가 존재하지 않습니다.")
    })
    @PostMapping("/member/{memberId}")
    SuccessResponse<Void> reportMember(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "memberId", description = "신고할 사용자 ID")
            @PathVariable("memberId") Long memberId,
            @Parameter(description = "사용자 신고 요청 정보")
            @RequestBody @Valid ReportMemberRequest request
    );
}
