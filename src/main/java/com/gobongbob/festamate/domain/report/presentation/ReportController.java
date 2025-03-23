package com.gobongbob.festamate.domain.report.presentation;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.report.application.ReportService;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{roomId}")
    public ResponseEntity<Void> reportRoom(
            @AuthenticationPrincipal Member member,
            @PathVariable Long roomId,
            @RequestBody ReportRoomRequest request
    ) {
        reportService.reportRoom(member, roomId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<ReportRoomResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/unprocessed")
    public ResponseEntity<List<ReportRoomResponse>> getUnprocessedReports() {
        return ResponseEntity.ok(reportService.getUnprocessedReports());
    }

    @PatchMapping("/{reportId}/process")
    public ResponseEntity<Void> processReport(@PathVariable Long reportId) {
        reportService.processReport(reportId);
        return ResponseEntity.ok().build();
    }
}