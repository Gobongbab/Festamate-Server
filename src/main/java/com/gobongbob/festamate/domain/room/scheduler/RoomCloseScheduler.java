package com.gobongbob.festamate.domain.room.scheduler;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomCloseScheduler {

    private final RoomRepository roomRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void closeExpiredRooms() {
        List<Room> expiredRooms = roomRepository.findRoomsByScheduledTimeBefore(LocalDateTime.now());
        expiredRooms.forEach(room -> room.updateStatus(Status.CLOSED));

        roomRepository.saveAll(expiredRooms); // 변경된 상태 저장
    }
}
