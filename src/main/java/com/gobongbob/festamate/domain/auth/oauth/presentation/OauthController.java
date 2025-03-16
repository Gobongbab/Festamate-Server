package com.gobongbob.festamate.domain.auth.oauth.presentation;

import com.gobongbob.festamate.domain.auth.oauth.application.OauthService;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

// 인가 코드를 받아 OauthService의 kakaoLogin 메서드를 호출하고, 인가 코드를 사용하여 액세스 토큰을 요청 후 사용자 정보를 가져와 처리함

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OauthController {

    private final OauthService oauthService;

    // @PostMapping을 통해 request로 인가 코드를 전달하고, response로 액세스 토큰을 받아옴
    @PostMapping("/kakao") // 발급받은 인가 코드를 전달 받음
    public ResponseEntity<Void> kakaoLogin(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request, HttpServletResponse response) {
        oauthService.kakaoLogin(loginRequest.getCode(), request,
                response); // loginRequest.getCode()를 통해 인가 코드 가져옴
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;

    @GetMapping("/login")
    public RedirectView redirectToKakao() {
        String kakaoAuthUrl =
                "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId + "&redirect_uri="
                        + redirectUri + "&response_type=code";
        return new RedirectView(kakaoAuthUrl);
    }
}
