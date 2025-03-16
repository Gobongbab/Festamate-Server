package com.gobongbob.festamate.domain.member.domain;

import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.room.domain.Room;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Getter
@Setter // 테스트 용으로 추후 삭제 바람
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

    public void setStudentInfo(String studentName, String studentDepartment, String studentId) {
        this.name = studentName;
        this.studentDepartment = studentDepartment;
        this.studentId = studentId;
    }

    public boolean isHost(Room room) {
        return room.getHost().equals(this);
    }


    /***
     * 아래부터 authorities, oauthInfo, accessToken 등의 필드가 추가됨.
     * Member 엔티티에 둘 지 아니면 다른 엔티티로 분리할 지 결정해야 함.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<SimpleGrantedAuthority> authorities;

    @Embedded
    private OauthInfo oauthInfo;

    private String kakaoAccessToken; // 카카오로부터  받아옴

    public Member(String id, String nickname, Set<SimpleGrantedAuthority> authorities) {
        this.id = Long.parseLong(id);
        this.nickname = nickname;
        this.authorities = authorities;
    }

    public Member update(String accessToken) {
        this.kakaoAccessToken = accessToken;
        return this;
    }

    public static Member createTestMember(Long id) {
        Member member = new Member();
        member.setId(id);
        return member;
    }

}
