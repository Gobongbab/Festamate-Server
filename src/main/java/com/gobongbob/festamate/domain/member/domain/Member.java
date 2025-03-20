package com.gobongbob.festamate.domain.member.domain;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.room.domain.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String nickname;

    @Column(unique = true)
    private String studentId;

    @Column(unique = true)
    private String loginId;

    private String loginPassword;

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Major major;

    private String studentDepartment; // 임시 필드, 학생증 등록으로 학과 정보 기입을 할 예정이면 이 필드를 사용. 추후 의논해야 함.

    @Column(unique = true)
    private String token; // FcmToken

    public void updateProfile(String nickname, String loginPassword) {
        this.nickname = nickname;
        this.loginPassword = loginPassword;
    }

    public void setStudentInfo(String studentName, String studentDepartment, String studentId) {
        this.name = studentName;
        this.studentDepartment = studentDepartment;
        this.studentId = studentId;
    }

    public boolean isHost(Room room) {
        return room.getHost().equals(this);
    }
}
