package com.gobongbob.festamate.domain.room.infrastructure;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RoomCloseEventListener {

    private final RoomRepository roomRepository;

    @EventListener
    @Transactional
    public void handleRoomCloseEvent(Room event) {
        roomRepository.findById(event.getId()).ifPresent(room -> {
            if (room.getStatus() != Status.CLOSED) {
                room.updateStatus(Status.CLOSED);
                roomRepository.save(room);
            }
        });
    }
}