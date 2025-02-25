package com.gobongbob.festamate.domain.room.application;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
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

    public List<RoomResponse> findAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(RoomResponse::toDto)
                .toList();
    }

    public RoomResponse findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .map(RoomResponse::toDto)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));
    }

    public void updateRoomById(Long roomId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));

        /*
        모임방 정보를 수정할 수 있는 조건인지 확인하는 로직이 추후 추가되어야 합니다.
        1. 요청한 사용자가 방장인지 확인한다.
        2. 방에 방장을 제외한 다른 사용자가 입장하지 않은 것을 확인한다.
         */

        room.updateRoom(
                request.headCount(),
                Gender.findByName(request.preferredGender()),
                request.openChatLink(),
                request.meetingDateTime(),
                request.title(),
                request.content()
        );

        roomRepository.save(room);
    }

    public void deleteRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("모임방이 존재하지 않습니다."));

        /*
        요청한 사용자가 방장인지 확인하는 로직이 추후 추가되어야 합니다.
         */

        roomRepository.delete(room);
    }
}
