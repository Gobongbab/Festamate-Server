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
        System.out.println("[scheduleRoomClose] roomId: " + meetingRoomId + ", delay(ms): " + delay);

        if (delay < 0) {
            System.out.println("[scheduleRoomClose] delay < 0 → 바로 closeRoom 실행");
            closeRoom(meetingRoomId);

            return;
        }

        scheduler.schedule(() -> {
            System.out.println("[ScheduledTask] 실행됨 → roomId: " + meetingRoomId);
            closeRoom(meetingRoomId);
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void closeRoom(Long roomId) {
        System.out.println("[closeRoom] 실행됨 → roomId: " + roomId);
        roomRepository.findById(roomId).ifPresent(room -> {
            System.out.println("[closeRoom] DB에서 조회됨 → roomId: " + roomId + ", status: " + room.getStatus());
            if (room.getStatus() != Status.CLOSED) {
                room.updateStatus(Status.CLOSED);
                System.out.println("[closeRoom] 상태 CLOSED로 변경 → roomId: " + roomId + ", status: " + room.getStatus());
                roomRepository.save(room);
                System.out.println("[closeRoom] 상태 CLOSED로 변경 후 save 완료 → roomId: " + roomId);
            } else {
                System.out.println("[closeRoom] 이미 CLOSED 상태 → roomId: " + roomId);
            }
        });
    }

}
