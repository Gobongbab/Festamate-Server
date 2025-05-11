package com.gobongbob.festamate.domain.room.scheduler;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomCloseEvent;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCloseScheduler {

    private final RoomRepository roomRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        List<Room> expiredRooms = roomRepository.findRoomsByScheduledTimeBefore(Status.MATCHING, LocalDateTime.now());
        expiredRooms.forEach(eventPublisher::publishEvent);

        List<Room> upcomingRooms = roomRepository.findRoomsByScheduledTimeAfter(Status.MATCHING, LocalDateTime.now());
        upcomingRooms.forEach(room -> scheduleRoomClose(room, room.getMeetingDateTime()));
    }

    public void scheduleRoomClose(Room room, LocalDateTime scheduledTime) {
        long delay = Duration.between(LocalDateTime.now(), scheduledTime).toMillis();

        scheduler.schedule(() -> {
            System.out.println("[Scheduler] 이벤트 발행 → roomId: " + room.getId());
            eventPublisher.publishEvent(new RoomCloseEvent(room));
        }, delay, TimeUnit.MILLISECONDS);
    }
}
