package com.gobongbob.festamate.domain.report.application;

import static com.gobongbob.festamate.global.response.ResponseCode.ALREADY_REPORT;
import static com.gobongbob.festamate.global.response.ResponseCode.CAN_NOT_REPORT_MYSELF;
import static com.gobongbob.festamate.global.response.ResponseCode.NOT_FOUND_ROOM;
import static com.gobongbob.festamate.global.response.ResponseCode.USER_NOT_FOUND;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.dto.request.ReportMemberRequest;
import com.gobongbob.festamate.domain.report.dto.request.ReportRoomRequest;
import com.gobongbob.festamate.domain.report.dto.response.ReportRoomResponse;
import com.gobongbob.festamate.domain.report.persistence.ReportRepository;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;

    // 방 신고하기
    @CheckActiveUser
    public void reportRoom(Member reporter, Long roomId, ReportRoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ROOM));

        Member host = memberRepository.findById(room.getHost().getId())
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND));

        // 자신의 방은 신고할 수 없음
        if (room.getHost().getId().equals(reporter.getId())) {
            throw new BadRequestException(CAN_NOT_REPORT_MYSELF);
        }

        // 이미 신고한 방인지 확인
        reportRepository.findByRoomIdAndReporterId(room.getId(), reporter.getId())
                .stream()
                .findAny()
                .ifPresent(report -> {
                    throw new BadRequestException(ALREADY_REPORT);
                });

        Report report = request.toEntity(reporter, room, host);
        reportRepository.save(report);
    }

    // 유저 신고하기
    @CheckActiveUser
    public void reportMember(Member reporter, Long memberId, ReportMemberRequest request) {
        Member reportedMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND));

        // 이미 신고한 유저인지 확인
        reportRepository.findByReportedMemberIdAndReporterId(reportedMember.getId(),
                        reporter.getId())
                .stream()
                .findAny()
                .ifPresent(report -> {
                    throw new BadRequestException(ALREADY_REPORT);
                });

        Report report = request.toEntity(reporter, reportedMember);
        reportRepository.save(report);
    }

    // 모든 신고 목록 조회
    @Transactional(readOnly = true)
    @CheckActiveUser
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
    @CheckActiveUser
    public List<ReportRoomResponse> getUnprocessedReports() {
        List<Report> reports = reportRepository.findByProcessed(false);
        List<ReportRoomResponse> responseList = new ArrayList<>();
        for (Report report : reports) {
            responseList.add(ReportRoomResponse.fromEntity(report));
        }
        return responseList;
    }
}