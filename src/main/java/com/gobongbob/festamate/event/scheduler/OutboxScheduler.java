package com.gobongbob.festamate.event.scheduler;

import static kotlinx.serialization.json.internal.JsonLexerKt.BATCH_SIZE;

import com.gobongbob.festamate.event.domain.OutboxEvent;
import com.gobongbob.festamate.event.exception.PermanentFailureException;
import com.gobongbob.festamate.event.persistence.OutboxEventRepository;
import com.gobongbob.festamate.event.processor.OutboxProcessor;
import com.gobongbob.festamate.global.config.OutboxProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxEventRepository outboxRepository;
    private final OutboxProcessor outboxProcessor;
    private final OutboxProperties outboxProperties;

    @Scheduled(fixedDelayString = "${outbox.schedule-delay}")
    public void pollAndProcessEvents() {

        int maxAttempts = outboxProperties.getMaxAttempts();
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        List<OutboxEvent> eventsToProcess = outboxRepository.findEventsToProcess(maxAttempts, pageable);

        if (eventsToProcess.isEmpty()) {
            return;
        }

        log.info("Outbox 이벤트 {}건 처리 시작", eventsToProcess.size());
        eventsToProcess.forEach(event -> {
            try {
                log.debug("Outbox ID {} 처리 위임", event.getId());
                outboxProcessor.processEvent(event);
            } catch (PermanentFailureException e) {
                log.error("최종 실패 이벤트 발생: {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Outbox ID {} 처리 중 시스템 오류 발생", event.getId(), e);
            }
        });
    }
}