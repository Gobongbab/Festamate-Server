package com.gobongbob.festamate.domain.room.domain;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.image.domain.RoomImage;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String title;

    private String place;

    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.MATCHING;

    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    private Gender preferredGender;

    private String preferredStudentIdMin;

    private String preferredStudentIdMax;

    private LocalDateTime meetingDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private Member host;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL)
    @Setter
    private ChatRoom chatRoom;

    // 연관관계 편의 메서드
    public void assignImages(List<RoomImage> roomImages) {
        this.images.addAll(roomImages);
        roomImages.forEach(roomImage -> roomImage.setRoom(this));
    }

    public void updateRoom(
            String title,
            String place,
            String content,
            Gender preferredGender,
            String preferredStudentIdMin,
            String preferredStudentIdMax,
            LocalDateTime meetingDateTime,
            int maxParticipants
    ) {
        this.title = title;
        this.place = place;
        this.content = content;
        this.preferredGender = preferredGender;
        this.preferredStudentIdMin = preferredStudentIdMin;
        this.preferredStudentIdMax = preferredStudentIdMax;
        this.meetingDateTime = meetingDateTime;
        this.maxParticipants = maxParticipants;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public boolean isFull() {
        return participants.size() == maxParticipants;
    }

    public boolean isJoinable() {
        return participants.size() == (maxParticipants / 2);
    }
}
