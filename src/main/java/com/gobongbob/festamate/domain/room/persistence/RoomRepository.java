package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long>, RoomQueryDslRepository {

    @Query("SELECT r FROM Room r WHERE r.status = ?1 AND r.meetingDateTime > ?2")
    List<Room> findRoomsByScheduledTimeAfter(Status status, LocalDateTime meetingDateTime);

    @Query("SELECT r FROM Room r WHERE r.status = ?1 AND r.meetingDateTime <= ?2")
    List<Room> findRoomsByScheduledTimeBefore(Status status, LocalDateTime meetingDateTime);

    Optional<Room> findById(Long id);

    @Query("SELECT r FROM Room r JOIN FETCH r.host WHERE r.id = :id")
    Optional<Room> findByIdWithHost(Long id);

    void delete(Room room);
}
