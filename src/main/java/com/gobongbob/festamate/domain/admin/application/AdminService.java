package com.gobongbob.festamate.domain.admin.application;

import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.LoginRequest;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.domain.Role;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // 관리자 로그인 로직
    @Transactional(readOnly = true)
    public Map<String, String> loginAdmin(LoginRequest loginRequest) {

        Member member = memberRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getLoginPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (member.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        // 토큰 생성 및 반환
        return tokenProvider.generateTokens(member, TokenType.ADMIN_ACCESS);
    }
}
