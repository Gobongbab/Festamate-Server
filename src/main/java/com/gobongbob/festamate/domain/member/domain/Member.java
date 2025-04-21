package com.gobongbob.festamate.domain.member.domain;

import static com.gobongbob.festamate.global.response.ResponseCode.NOT_ENOUGH_TICKET;

import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long kakaoId; // 카카오 고유 ID로 기존 사용자와 구분하기 위해 사용

    @Column(nullable = false)
    @Builder.Default
    private boolean isProfileCompleted = false; // 첫 로그인 후 프로필 작성 여부 판단용

    public void completeProfile() {
        this.isProfileCompleted = true;
    }

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

    private String studentDepartment; // 임시 필드, 학생증 등록으로 학과 정보 기입을 할 예정이면 이 필드를 사용. 추후 의논해야 함.

    @Column(unique = true)
    private String token; // FcmToken

    @Builder.Default
    private int maximumTicket = 2;

    @Builder.Default
    private int remainingTicket = 2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    public void registerProfile(
            String name,
            String nickname,
            String studentId,
            String phoneNumber,
            Gender gender,
            String studentDepartment
    ) {
        this.name = name;
        this.nickname = nickname;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.studentDepartment = studentDepartment;
    }

    // Enum으로 바꿀 예정
    @Getter
    private String role; // 역할

    public static class MemberBuilder {

        public MemberBuilder role(String role) {
            this.role = role;
            return this;
        }
    }

    public void initializeProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
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
        return "ADMIN".equals(this.role) || room.getHost().getId().equals(this.getId());
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

    public enum MemberStatus {
        ACTIVE, // 제재 해제
        BLOCKED // 제재
    }

    @PrePersist
    public void setDefaultStatus() {
        if (status == null) {
            this.status = MemberStatus.ACTIVE;
        }
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    public void block() {
        this.status = MemberStatus.BLOCKED;
    }

    public void unblock() {
        this.status = MemberStatus.ACTIVE;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
