package com.gobongbob.festamate.event.persistence;

import com.gobongbob.festamate.event.domain.OutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e "
            + "FROM OutboxEvent e "
            + "WHERE e.status = 'WAITING' OR (e.status = 'FAILED' AND e.failCount < :maxAttempts) "
            + "ORDER BY e.id ASC")
    List<OutboxEvent> findRetriableEvents(@Param("maxAttempts") int maxAttempts);
}
