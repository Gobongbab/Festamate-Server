package com.gobongbob.festamate.domain.admin.application;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.jwt.dto.request.LoginRequest;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.domain.Role;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // 관리자 로그인 로직
    @Transactional
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
        return tokenService.generateAndSaveTokens(member.getId(), TokenType.ADMIN_ACCESS);
    }
}
