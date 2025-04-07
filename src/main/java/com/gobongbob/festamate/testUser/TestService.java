package com.gobongbob.festamate.testUser;

import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public TestTokens createTestMember() {
        // 테스트용 Member 객체 생성
        Member testMember = Member.builder()
                .name("Test User")
                .nickname("test_nickname")
                .studentId("test_student_id")
                .loginId("test_login_id")
                .loginPassword("test_password")
                .phoneNumber("010-0000-0000")
                .profileImage(null)
                .gender(Gender.MALE)
                .major(Major.COMPUTER_SCIENCE)
                .role("USER")
                .build();

        // 데이터베이스에 저장
        memberRepository.save(testMember);

        // 테스트용 Access Token 및 Refresh Token 생성
        String accessToken = tokenProvider.generateTestAccessToken(testMember);
        String refreshToken = tokenProvider.generateTestRefreshToken(testMember);

        return new TestTokens(accessToken, refreshToken);
    }

    @Transactional
    public TestTokens createAdminMember() {
        ProfileImage profileImage = ProfileImage.getDefaultProfileImage(); // 기본 프로필 이미지 사용

        // 관리자용 Member 객체 생성
        Member adminMember = Member.builder()
                .name("Admin User")
                .nickname("admin_nickname")
                .studentId("admin_student_id")
                .loginId("admin_login_id")
                .loginPassword("admin_password")
                .phoneNumber("010-1111-1111")
                .profileImage(profileImage)
                .gender(Gender.MALE)
                .major(Major.COMPUTER_SCIENCE)
                .role("ADMIN") // 관리자 역할
                .build();

        // 데이터베이스에 저장
        memberRepository.save(adminMember);

        // 관리자용 토큰 생성
        String accessToken = tokenProvider.generateAdminAccessToken(adminMember);
        String refreshToken = tokenProvider.generateAdminRefreshToken(adminMember);

        return new TestTokens(accessToken, refreshToken);
    }

    public static class TestTokens {

        private final String accessToken;
        private final String refreshToken;

        public TestTokens(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
