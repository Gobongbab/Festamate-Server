package com.gobongbob.festamate.domain.image.domain;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE room_image SET deleted = true, deleted_at = now() WHERE id = ?")
public class RoomImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    // 연관관계 편의 메서드
    public void setRoom(Room room) {
        this.room = room;
    }

    public static RoomImage fromEntity(Image image) {
        return RoomImage.builder()
                .image(image)
                .build();
    }
}
