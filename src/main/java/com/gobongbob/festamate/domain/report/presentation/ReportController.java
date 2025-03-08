package com.gobongbob.festamate.domain.report.presentation;

import com.gobongbob.festamate.domain.report.application.ReportService;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{roomId}")
    public ResponseEntity<Void> reportRoom(
            @RequestBody ReportRoomRequest request,
            @PathVariable Long roomId
    ) {
        Long reporterId = 1L;  // 추후 Spring Security를 활용하여 사용자 정보를 가져오도록 변경 필요
        reportService.reportRoom(request, roomId, reporterId);
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