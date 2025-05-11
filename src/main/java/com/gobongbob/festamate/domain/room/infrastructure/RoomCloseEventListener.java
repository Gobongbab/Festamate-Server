package com.gobongbob.festamate.domain.room.infrastructure;

import com.gobongbob.festamate.domain.room.domain.RoomCloseEvent;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RoomCloseEventListener {

    private final RoomRepository roomRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomCloseEvent(RoomCloseEvent event) {
        roomRepository.findById(event.getRoomId()).ifPresent(room -> {
            if (room.getStatus() != Status.CLOSED) {
                room.updateStatus(Status.CLOSED);
                roomRepository.save(room);
                System.out.println("[RoomEventListener] 상태 변경 완료 → roomId: " + room.getId());
                System.out.println("[RoomEventListener] 상태 변경 완료 → room.getStatus(): " + room.getStatus());
            }
        });
    }
}