package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.room.domain.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<Room, Long> {

    Room save(Room room);

    List<Room> findAll();

    Optional<Room> findById(Long id);

    void delete(Room room);
}
