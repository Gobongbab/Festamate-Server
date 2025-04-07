package com.gobongbob.festamate.domain.report.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.report.application.ReportService;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import java.util.List;

import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{roomId}")
    public SuccessResponse<Void> reportRoom(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @PathVariable Long roomId,
            @RequestBody @Valid ReportRoomRequest request
    ) {
        reportService.reportRoom(memberDetails.getMember(), roomId, request);
        return new SuccessResponse<>();
    }

    @GetMapping("")
    public SuccessResponse<List<ReportRoomResponse>> getAllReports() {
        return new SuccessResponse<>(reportService.getAllReports());
    }

    @GetMapping("/unprocessed")
    public SuccessResponse<List<ReportRoomResponse>> getUnprocessedReports() {
        return new SuccessResponse<>(reportService.getUnprocessedReports());
    }

    @PatchMapping("/{reportId}/process")
    public SuccessResponse<Void> processReport(@PathVariable Long reportId) {
        reportService.processReport(reportId);
        return new SuccessResponse<>();
    }
}