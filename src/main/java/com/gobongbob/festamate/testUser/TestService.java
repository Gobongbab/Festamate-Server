package com.gobongbob.festamate.testUser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.domain.Role;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * 테스트용 회원 생성 및 토큰 반환 이미 존재하는 경우 해당 회원의 토큰 반환 (기본적으로는 매번 새로 생성 시도)
     */
    @Transactional // DB 저장 및 토큰 생성/저장 로직 포함
    public Map<String, String> createTestMemberAndGetTokens() {
        // 고유 식별자를 사용하여 중복 방지 (예: 타임스탬프)
        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 100000000); // 조금 더 짧게
        String nickname = "수영하는 봉밥이" + uniqueSuffix;
        // 학번 형식 유지하며 고유하게 생성 (예시 단순화)
        String studentId = String.format("%d%05d", (2018 + (int) (Math.random() * 8)),
                (int) (Math.random() * 100000));
        String loginId = "test_login_" + uniqueSuffix;
        // 전화번호 형식 유지하며 고유하게 생성 (마지막 8자리를 타임스탬프 일부로 사용)
        String phoneNumber = "010-" + uniqueSuffix.substring(uniqueSuffix.length() - 8,
                uniqueSuffix.length() - 4) + "-" + uniqueSuffix.substring(
                uniqueSuffix.length() - 4);
        // kakaoId도 고유해야 함
        Long kakaoId = System.currentTimeMillis(); // 현재 시간 사용

        // Member 엔티티 생성
        Gender randomGender = Math.random() < 0.5 ? Gender.MALE : Gender.FEMALE;

        // 비밀번호 암호화 적용
        String rawPassword = "test_password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Member testMember = createMemberEntity(
                "수영하는 봉밥이 " + uniqueSuffix,
                nickname,
                studentId,
                loginId,
                encodedPassword,
                phoneNumber,
                kakaoId,
                randomGender,
                "컴퓨터 공학과",
                Role.USER // 테스트 유저는 USER 역할
        );

        // 초기 프로필 이미지 설정 (DB에 해당 이름의 이미지가 있다고 가정)
        String profileImageName = "swimBong.png";
        profileImageRepository.findByStoreName(profileImageName)
                .ifPresent(testMember::initializeProfileImage);

        // DB에 회원 저장
        Member savedMember = memberRepository.save(testMember);

        // TokenService를 사용하여 토큰 생성 및 Redis 저장까지 한번에 처리
        Map<String, String> tokens = tokenService.generateAndSaveTokens(savedMember.getId(),
                TokenType.TEST_ACCESS);

        return tokens; // accessToken과 refreshToken이 담긴 Map 반환
    }

    /**
     * 관리자 회원 생성 및 토큰 반환 이미 존재하는 경우 해당 회원의 토큰 반환 (기본적으로는 매번 새로 생성 시도)
     */
    // 테스트 관리자 비활성화
    @Transactional
    public TestTokens createAdminMember() {
        // 고유 식별자를 사용하여 중복 방지
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String nickname = "관리자 봉밥이" + uniqueSuffix;
        String studentId = String.format("%d%05d", (2018 + (int) (Math.random() * 8)),
                (int) (Math.random() * 100000));
        String loginId = "admin_login_" + uniqueSuffix;
        // 전화번호 형식 유지하며 고유하게 생성
        String phoneNumber = "010-" + uniqueSuffix.substring(uniqueSuffix.length() - 8,
                uniqueSuffix.length() - 4) + "-" + uniqueSuffix.substring(
                uniqueSuffix.length() - 4);
        // kakaoId도 고유해야 함
        Long kakaoId = Long.parseLong(uniqueSuffix);

        // Member 엔티티 생성
        Gender randomGender = Math.random() < 0.5 ? Gender.MALE : Gender.FEMALE;

        // 비밀번호 암호화 적용
        String rawPassword = "admin_password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Member adminMember = createMemberEntity(
                "Admin User " + uniqueSuffix,
                nickname,
                studentId,
                loginId,
                encodedPassword,
                phoneNumber,
                kakaoId,
                randomGender,
                "컴퓨터 공학과",
                Role.ADMIN// 관리자는 ADMIN 역할 부여
        );

        return createAndSaveMember(adminMember, TokenType.ADMIN_ACCESS);
    }

    /**
     * 공통 로직: 회원 엔티티 생성, 프로필 이미지 설정, 저장 및 토큰 생성
     */
    private TestTokens createAndSaveMember(Member member, TokenType tokenType) {

        String profileImageName = "swimBong.png";
        if (tokenType == TokenType.ADMIN_ACCESS) {
            profileImageName = "adminBong.png";
        }

        // 초기 프로필 이미지 설정 (DB에 기본 이미지가 있다고 가정)
        profileImageRepository.findByStoreName(profileImageName)
                .ifPresent(member::initializeProfileImage);

        // DB에 회원 저장
        Member savedMember = memberRepository.save(member);

        // 토큰 생성
        Map<String, String> tokens = tokenProvider.generateTokens(savedMember, tokenType);

        // Map에서 accessToken과 refreshToken을 추출
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        return new TestTokens(accessToken, refreshToken);
    }

    /**
     * Member 엔티티 생성을 위한 헬퍼 메서드 Member 엔티티의 @Unique 제약 조건 필드들을 파라미터로 받음
     */
    private Member createMemberEntity(String name, String nickname, String studentId,
            String loginId, String encodedPassword, String phoneNumber, Long kakaoId, Gender gender,
            String department, Role role) {
        return Member.builder()
                .name(name)
                .nickname(nickname)          // Unique
                .studentId(studentId)        // Unique
                .loginId(loginId)            // Unique
                .loginPassword(encodedPassword)     // 비밀번호 암호화
                .phoneNumber(phoneNumber)    // Unique
                .kakaoId(kakaoId)            // Unique
                .gender(gender)
                .studentDepartment(department)
                .role(role)                  // 역할 명시적 설정
                .isProfileCompleted(true)    // 테스트 유저는 프로필 작성이 완료된 것으로 가정
                .maximumTicket(1000) // 테스트 유저는 티켓 1000장으로 설정
                .remainingTicket(1000) // 테스트 유저는 티켓 1000장으로 설정
                // .status(MemberStatus.ACTIVE) // Builder.Default 또는 @PrePersist로 설정됨
                .build();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true) // 불필요한 필드가 직렬화되지 않도록
    public static class TestTokens {

        private final String accessToken;
        private final String refreshToken;

        public TestTokens(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
