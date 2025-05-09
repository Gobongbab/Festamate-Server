package com.gobongbob.festamate.domain.chat.domain;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Builder.Default
    private String lastMessageContent = "";

    private LocalDateTime lastMessageTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public static ChatRoom createChatRoom(String title, Room room) {
        ChatRoom chatRoom = ChatRoom.builder()
                .title(title)
                .build();
        chatRoom.setRoom(room);

        return chatRoom;
    }

    // 연관관계 편의 메서드
    public void setRoom(Room room) {
        this.room = room;
        room.setChatRoom(this);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateLastMessage(String content) {
        this.lastMessageContent = content;
        this.lastMessageTime = LocalDateTime.now();
    }
}