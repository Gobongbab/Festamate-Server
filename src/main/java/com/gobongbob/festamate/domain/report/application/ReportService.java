package com.gobongbob.festamate.domain.report.application;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import com.gobongbob.festamate.domain.report.persistence.ReportRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;

    // 신고하기
    public void reportRoom(ReportRoomRequest request, Long roomId, Long reporterId) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("신고할 방이 존재하지 않습니다."));

        // 자신의 방은 신고할 수 없음
        if (room.getHost().getId().equals(reporterId)) {
            throw new IllegalArgumentException("자신의 방은 신고할 수 없습니다.");
        }

        // 이미 신고한 방인지 확인
        List<Report> existingReports = reportRepository.findByRoomIdAndReporterId(room.getId(), reporterId);
        if (!existingReports.isEmpty()) {
            throw new IllegalArgumentException("이미 신고한 방입니다.");
        }

        Report report = request.toEntity(reporter, room);

        reportRepository.save(report);
    }

    // 모든 신고 목록 조회
    @Transactional(readOnly = true)
    public List<ReportRoomResponse> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        List<ReportRoomResponse> responseList = new ArrayList<>();
        for (Report report : reports) {
            responseList.add(ReportRoomResponse.fromEntity(report));
        }
        return responseList;
    }

    // 신고 처리되지 않은 신고 목록 조회
    @Transactional(readOnly = true)
    public List<ReportRoomResponse> getUnprocessedReports() {
        List<Report> reports = reportRepository.findByProcessed(false);
        List<ReportRoomResponse> responseList = new ArrayList<>();
        for (Report report : reports) {
            responseList.add(ReportRoomResponse.fromEntity(report));
        }
        return responseList;
    }

    // 신고 처리
    public void processReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다."));

        report.markAsProcessed();
        reportRepository.save(report);
    }
}