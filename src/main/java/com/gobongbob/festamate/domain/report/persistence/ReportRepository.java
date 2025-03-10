package com.gobongbob.festamate.domain.report.persistence;

import com.gobongbob.festamate.domain.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByProcessed(Boolean processed);
    List<Report> findByRoomIdAndReporterId(Long roomId, Long reporterId);
}
