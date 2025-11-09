package com.gobongbob.festamate.event.processor;

import com.gobongbob.festamate.event.domain.OutboxEvent;
import com.gobongbob.festamate.event.exception.PermanentFailureException;
import com.gobongbob.festamate.event.exception.RetryableException;
import com.gobongbob.festamate.event.handler.EventHandler;
import com.gobongbob.festamate.event.persistence.OutboxEventRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final Map<String, EventHandler> eventHandlerMap;
    private final OutboxEventRepository outboxEventRepository; // Repository 주입

    @Value("${outbox.max.attempts:5}")
    private int MAX_ATTEMPTS;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(OutboxEvent detachedEvent) {

        Optional<OutboxEvent> optionalEvent = outboxEventRepository.findById(detachedEvent.getId());

        if (optionalEvent.isEmpty()) {
            log.warn("Outbox ID {}를 처리하려 했으나 DB에 존재하지 않습니다. (아마도 중복 처리됨)", detachedEvent.getId());
            return;
        }

        OutboxEvent event = optionalEvent.get();

        if (event.isExceededMaxAttempts(MAX_ATTEMPTS)) {
            log.warn("Outbox ID {}가 최대 재시도 횟수({})를 초과하여 영구 실패 처리합니다.", event.getId(), MAX_ATTEMPTS);
            event.markPermanentFailed();
            return;
        }

        EventHandler handler = eventHandlerMap.get(event.getMessageType());
        if (handler == null) {
            log.error("알 수 없는 이벤트 타입 '{}'. 영구 실패 처리합니다. ID={}", event.getMessageType(), event.getId());
            event.markPermanentFailed();
            return;
        }

        try {
            handler.handle(event.getPayload());
            event.markDone();
            log.info("Outbox 이벤트 처리 성공: ID={}", event.getId());
        } catch (RetryableException e) {
            event.markFailed();
            log.warn("Outbox 이벤트 처리 일시적 실패 (재시도 예정): ID={}, 실패 횟수={}", event.getId(), event.getFailCount(), e);
        } catch (PermanentFailureException e) {
            event.markPermanentFailed();
            log.error("Outbox 이벤트 처리 영구 실패: ID={}", event.getId(), e);
        } catch (Exception e) {
            event.markFailed();
            log.error("Outbox 이벤트 처리 중 예상치 못한 오류 발생 (재시도 예정): ID={}, 실패 횟수={}", event.getId(), event.getFailCount(), e);
        }
    }
}