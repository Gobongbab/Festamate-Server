package com.gobongbob.festamate.domain.room.repository;

import com.gobongbob.festamate.domain.room.domain.Room;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface RoomRepository extends Repository<Long, Room> {
    Room save(Room room);

    Optional<Room> findById(Long id);

    void deleteById(Long roomId);
}
