package com.gobongbob.festamate.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_event")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String messageType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private EventStatus status = EventStatus.WAITING;

    @Builder.Default
    @Column(nullable = false)
    private int failCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    private LocalDateTime processedAt;
    private LocalDateTime failedAt;

    public void markDone() {
        this.status = EventStatus.DONE;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = EventStatus.FAILED;
        this.failCount += 1;
        this.failedAt = LocalDateTime.now();
    }

    public void markPermanentFailed() {
        this.status = EventStatus.PERMANENT_FAILED;
    }

    public boolean isExceededMaxAttempts(int maxAttempts) {
        return this.failCount >= maxAttempts;
    }
}