//package com.gobongbob.festamate.domain.admin.presentation;
//
//import com.gobongbob.festamate.domain.image.domain.ProfileImage;
//import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
//import com.gobongbob.festamate.domain.member.domain.Gender;
//import com.gobongbob.festamate.domain.member.domain.Member;
//import com.gobongbob.festamate.domain.member.domain.Role;
//import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
//import java.util.Optional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class AdminInitializer implements ApplicationRunner {
//
//    private final MemberRepository memberRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final ProfileImageRepository profileImageRepository;
//
//    @Value("${admin.loginId}")
//    private String adminLoginId;
//
//    @Value("${admin.password}")
//    private String adminRawPassword; // 암호화되지 않은 원본 비밀번호
//
//    @Value("${admin.kakaoId}")
//    private Long adminKakaoId;
//
//    @Override
//    @Transactional
//    public void run(ApplicationArguments args) {
//
//        boolean adminExists = memberRepository.existsByLoginId(adminLoginId);
//        if (!adminExists) {
//            // 환경 변수에서 읽어온 비밀번호 암호화
//            String encodedPassword = passwordEncoder.encode(adminRawPassword);
//
//            // 기본 프로필 이미지 찾기 및 설정
//            Optional<ProfileImage> defaultAdminImage = profileImageRepository.findByStoreName(
//                    "adminBong.png"); // 관리자용 기본 이미지 이름
//            ProfileImage adminImage = defaultAdminImage.orElse(null); // 없으면 null
//
//            // Member 엔티티 생성 (환경 변수 값 사용)
//            Member admin = Member.builder()
//                    .loginId(adminLoginId)
//                    .loginPassword(encodedPassword)
//                    .nickname("관리자 봉밥이")
//                    .name("관리자 고봉밥")
//                    .role(Role.ADMIN)
//                    .isProfileCompleted(true)
//                    .studentId("000000000")
//                    .phoneNumber("010-0000-0000")
//                    .kakaoId(adminKakaoId)
//                    .gender(Gender.MALE)
//                    .studentDepartment("관리부서")
//                    .profileImage(adminImage)
//                    .maximumTicket(1000)
//                    .remainingTicket(1000)
//                    .build();
//
//            memberRepository.save(admin);
//        }
//    }
//}
