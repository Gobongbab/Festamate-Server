package com.gobongbob.festamate.domain.room.domain;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
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

    private String openChatLink;

    private LocalDateTime meetingDateTime;

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private Member host;

    public void updateRoom(
            int headCount,
            Gender preferredGender,
            String openChatLink,
            LocalDateTime meetingDateTime,
            String title,
            String content
    ) {
        this.headCount = headCount;
        this.preferredGender = preferredGender;
        this.openChatLink = openChatLink;
        this.meetingDateTime = meetingDateTime;
        this.title = title;
        this.content = content;
    }
}
