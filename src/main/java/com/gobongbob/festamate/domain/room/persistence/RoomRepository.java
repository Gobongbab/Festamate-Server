package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.room.domain.Room;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<Room, Long> {

    Room save(Room room);

    Page<Room> findAll(Pageable pageable);

    Optional<Room> findById(Long id);

    @Query("SELECT r FROM Room r JOIN FETCH r.host WHERE r.id = :id")
    Optional<Room> findByIdWithHost(Long id);

    void delete(Room room);
}
