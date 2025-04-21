package com.gobongbob.festamate.testUser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final TokenProvider tokenProvider;

    // 테스트용 회원 생성 및 토큰 반환
    @Transactional
    public TestTokens createTestMember() {
        // 테스트용 회원 생성
        Member testMember = createTestMemberEntity("Test User7", "test_nickname7",
                "test_student_id7");

        // 초기 프로필 이미지 설정
        profileImageRepository.findByStoreName("default_profile_image.png")
                .ifPresent(testMember::initializeProfileImage);

        // DB에 회원 저장
        memberRepository.save(testMember);  // JPA를 사용하여 DB에 저장

        // generateTokens() 메서드가 Map<String, String>을 반환하므로 이를 사용
        Map<String, String> tokens = tokenProvider.generateTokens(testMember,
                TokenType.TEST_ACCESS);

        // Map에서 accessToken과 refreshToken을 추출
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        return new TestTokens(accessToken, refreshToken);
    }

    // 관리자 회원 생성 및 토큰 반환
    @Transactional
    public TestTokens createAdminMember() {
        // 관리자 회원 생성
        Member adminMember = createAdminMemberEntity("Admin User", "admin_nickname",
                "admin_student_id");

        // 초기 프로필 이미지 설정
        profileImageRepository.findByStoreName("default_profile_image.png")
                .ifPresent(adminMember::initializeProfileImage);

        // DB에 관리자 회원 저장
        memberRepository.save(adminMember);  // 관리자 회원을 DB에 저장

        // 토큰 생성
        Map<String, String> tokens = tokenProvider.generateTokens(adminMember,
                TokenType.ADMIN_ACCESS);

        // Map에서 accessToken과 refreshToken을 추출
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // 반환
        return new TestTokens(accessToken, refreshToken);
    }

    private Member createTestMemberEntity(String name, String nickname, String studentId) {
        return Member.builder()
                .name(name)
                .nickname(nickname)
                .studentId(studentId)
                .loginId("test_login_id")
                .loginPassword("test_password")
                .phoneNumber("010-7777-7777")
                .gender(Gender.MALE)
                .studentDepartment("컴퓨터 공학부")
                .build();
    }

    private Member createAdminMemberEntity(String name, String nickname, String studentId) {
        return Member.builder()
                .name(name)
                .nickname(nickname)
                .studentId(studentId)
                .loginId("admin_login_id")
                .loginPassword("admin_password")
                .phoneNumber("010-1111-1111")
                .gender(Gender.MALE)
                .studentDepartment("컴퓨터 공학부")
                .role("ADMIN")
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
