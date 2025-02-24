package com.gobongbob.festamate.domain.room.service;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public void createRoom(RoomCreateRequest request) {
        Room room = request.toEntity();

        roomRepository.save(room);
    }

    public List<RoomResponse> findRooms() {
        return roomRepository.findAll()
                .stream()
                .map(RoomResponse::toDto)
                .toList();
    }
}
