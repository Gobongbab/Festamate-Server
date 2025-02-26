package com.gobongbob.festamate.domain.room.application;

import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.serviceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("모임방 관련 기능 테스트")
class RoomServiceTest extends serviceSliceTest {

    @Autowired
    RoomService roomService;
    @Autowired
    RoomRepository roomRepository;
}
