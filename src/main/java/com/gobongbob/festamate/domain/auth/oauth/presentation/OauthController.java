package com.gobongbob.festamate.domain.auth.oauth.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.oauth.application.OauthService;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.LoginRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.LoginWithKakaoRequest;
import com.gobongbob.festamate.domain.auth.oauth.dto.response.KakaoCheckResponse;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// 소그인 핵심 로직
// 인가 코드를 받아 OauthService의 kakaoLogin 메서드를 호출하고, 인가 코드를 사용하여 액세스 토큰을 요청 후 사용자 정보를 가져와 처리함

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OauthController {

    private final OauthService oauthService;
    private final MemberService memberService;
    private final TokenService tokenService;

    // 1️⃣ 회원 여부 확인용 (카카오 인가코드로)
    @PostMapping("/kakao")
    public SuccessResponse<KakaoCheckResponse> kakaoCheck(
            @RequestBody @Valid LoginRequest loginRequest) {

        KakaoCheckResponse response = oauthService.checkKakaoUser(loginRequest.getCode());
        return new SuccessResponse<>(response);
    }

    // 2️⃣ 기존 회원 로그인 (카카오 access token으로)
    @PostMapping("/login")
    public SuccessResponse<Map<String, String>> loginWithKakao(
            @RequestBody LoginWithKakaoRequest request) {
        Map<String, String> tokens = oauthService.loginWithKakao(request.getKakaoAccessToken());
        return new SuccessResponse<>(tokens);
    }

    // 3️⃣ 신규 회원 프로필 등록 후 JWT 발급
    @PostMapping("/register/profile")
    @Transactional // 이 메서드 전체를 하나의 트랜잭션으로 묶음
    public SuccessResponse<Map<String, String>> registerProfile(
            @RequestBody ProfileRegisterRequest request) {

        Long userId = oauthService.registerNewMember(request); // 같은 트랜잭션 내에서 실행
        Map<String, String> tokens = tokenService.generateTokens(userId,
                TokenType.FINAL_ACCESS); // 같은 트랜잭션 내에서 실행
        return new SuccessResponse<>(tokens);
    } // 메서드 종료 시 트랜잭션 커밋
}
