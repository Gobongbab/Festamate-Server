package com.gobongbob.festamate.domain.roomParticipant.persistence;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.roomParticipant.domain.RoomParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomParticipantRepository extends Repository<RoomParticipant, Long> {

    RoomParticipant save(RoomParticipant roomParticipant);

    List<RoomParticipant> saveAll(List<RoomParticipant> roomParticipants);

    Optional<RoomParticipant> findById(Long id);

    void delete(RoomParticipant roomParticipant);

    void deleteByRoom(Room room);

    List<RoomParticipant> findByRoom_Id(Long id);

    List<RoomParticipant> findByMember_Id(Long id);
}
