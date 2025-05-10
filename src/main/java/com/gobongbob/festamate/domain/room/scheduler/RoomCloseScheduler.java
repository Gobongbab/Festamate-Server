package com.gobongbob.festamate.domain.room.scheduler;

import com.gobongbob.festamate.domain.room.domain.Room;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCloseScheduler {

    private final RoomRepository roomRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        List<Room> upcomingRooms = roomRepository.findRoomsByScheduledTimeAfter(
                Status.MATCHING,
                LocalDateTime.now()
        );

        upcomingRooms.forEach(room -> scheduleRoomClose(room.getId(), room.getMeetingDateTime()));
    }

    public void scheduleRoomClose(Long meetingRoomId, LocalDateTime scheduledTime) {
        long delay = Duration.between(LocalDateTime.now(), scheduledTime).toMillis();
        if (delay < 0) { // 이미 시간이 지났다면 바로 닫기
            closeRoom(meetingRoomId);

            return;
        }

        scheduler.schedule(() -> closeRoom(meetingRoomId), delay, TimeUnit.MILLISECONDS);
    }

    private void closeRoom(Long roomId) {
        roomRepository.findById(roomId)
                .ifPresent(room -> {
                    if (room.getStatus() != Status.CLOSED) {
                        room.updateStatus(Status.CLOSED);
                        roomRepository.save(room);
                    }
                });
    }
}
