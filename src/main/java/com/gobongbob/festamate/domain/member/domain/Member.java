package com.gobongbob.festamate.domain.member.domain;

import com.gobongbob.festamate.domain.major.domain.Major;
import jakarta.persistence.*;
import lombok.*;

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

    public void updateProfile(String nickname, String loginPassword) {
        this.nickname = nickname;
        this.loginPassword = loginPassword;
    }

    public void setStudentInfo( String studentName, String studentDepartment, String studentId) {
        this.name = studentName;
        this.studentDepartment = studentDepartment;
        this.studentId = studentId;
    }
}
