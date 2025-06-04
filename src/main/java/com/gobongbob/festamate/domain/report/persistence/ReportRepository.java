package com.gobongbob.festamate.domain.report.persistence;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.report.domain.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByProcessed(Boolean processed);

    List<Report> findByRoomIdAndReporterId(Long roomId, Long reporterId);

    List<Report> findByReportedMemberIdAndReporterId(Long reportedMemberId, Long reporterId);

    List<Report> findByReportedMember(Member member);
}
