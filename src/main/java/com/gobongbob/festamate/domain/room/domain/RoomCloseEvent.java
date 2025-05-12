package com.gobongbob.festamate.domain.room.domain;

public class RoomCloseEvent {

    private final Long roomId;

    public RoomCloseEvent(Room room) {
        this.roomId = room.getId();
    }

    public Long getRoomId() {
        return roomId;
    }
}
