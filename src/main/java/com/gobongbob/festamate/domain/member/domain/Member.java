package com.gobongbob.festamate.domain.member.domain;

import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static com.gobongbob.festamate.global.response.ResponseCode.NOT_ENOUGH_TICKET;

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
    private String loginId; // 관리자 일반 로그인 관련 필드

    private String loginPassword; // 관리자 일반 로그인 관련 필드

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Major major;

    private String studentDepartment; // 임시 필드, 학생증 등록으로 학과 정보 기입을 할 예정이면 이 필드를 사용. 추후 의논해야 함.

    @Column(unique = true)
    private String token; // FcmToken

    @Builder.Default
    private int maximumTicket = 2;

    @Builder.Default
    private int remainingTicket = 2;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    public void registerProfile(
            String name,
            String nickname,
            String studentId,
            String phoneNumber,
            Gender gender,
            Major major
    ) {
        this.name = name;
        this.nickname = nickname;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.major = major;
    }

    public void initializeProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    public void updateProfile(String nickname, String loginPassword) {
        this.nickname = nickname;
        this.loginPassword = loginPassword;
    }

    public void setStudentInfo(String studentName, String studentDepartment, String studentId) {
        this.name = studentName;
        this.studentDepartment = studentDepartment;
        this.studentId = studentId;
    }

    public void initializeRemainingTicket(int ticketCount) {
        this.remainingTicket = ticketCount;
    }

    public void useTicket() {
        if (this.remainingTicket > 0) {
            this.remainingTicket--;
        } else {
            throw new BadRequestException(NOT_ENOUGH_TICKET);
        }
    }

    public void initTicket() {
        this.remainingTicket = maximumTicket;
    }

    public void increaseMaximumTicket() {
        this.maximumTicket++;
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
        return Member.builder()
                .id(id)
                .build();
    }

}
