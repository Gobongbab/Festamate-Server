package com.gobongbob.festamate.domain.room.domain;

import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int headCount;

    private Gender preferredGender;

    private LocalDateTime meetingDateTime;

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private Member host;

    @OneToMany(mappedBy = "room", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<RoomImage> images = new ArrayList<>();

    // 연관관계 편의 메서드
    public void assignImages(List<RoomImage> roomImages) {
        this.images = roomImages;
        roomImages.forEach(roomImage -> roomImage.setRoom(this));
    }

    public void updateRoom(
            int headCount,
            Gender preferredGender,
            LocalDateTime meetingDateTime,
            String title,
            String content
    ) {
        this.headCount = headCount;
        this.preferredGender = preferredGender;
        this.meetingDateTime = meetingDateTime;
        this.title = title;
        this.content = content;
    }
}
