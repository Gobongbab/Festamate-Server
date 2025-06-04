package com.gobongbob.festamate.domain.report.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.report.application.ReportService;
import com.gobongbob.festamate.domain.report.dto.request.ReportMemberRequest;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController implements ReportApi {

    private final ReportService reportService;

    // 모임방 신고
    @Override
    @PostMapping("/room/{roomId}")
    public SuccessResponse<Void> reportRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("roomId") Long roomId,
            @RequestBody @Valid ReportRoomRequest request
    ) {
        reportService.reportRoom(memberDetails.getMember(), roomId, request);
        return new SuccessResponse<>();
    }

    // 사용자 신고
    @Override
    @PostMapping("/member/{memberId}")
    public SuccessResponse<Void> reportMember(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable("memberId") Long memberId,
            @RequestBody @Valid ReportMemberRequest request
    ) {
        reportService.reportMember(memberDetails.getMember(), memberId, request);
        return new SuccessResponse<>();
    }
}
