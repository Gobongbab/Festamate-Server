package com.gobongbob.festamate.domain.auth.oauth.presentation;

import com.gobongbob.festamate.domain.auth.oauth.application.OauthService;
import com.gobongbob.festamate.domain.auth.oauth.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 인가 코드를 받아 OauthService의 kakaoLogin 메서드를 호출하고, 인가 코드를 사용하여 액세스 토큰을 요청 후 사용자 정보를 가져와 처리함

@RestController
@RequiredArgsConstructor
@RequestMapping
public class OauthController {

    private final OauthService oauthService;

    // @PostMapping을 통해 request로 인가 코드를 전달하고, response로 액세스 토큰을 받아옴
    // 수동으로 인가 코드를 전달받아 처리 (테스트용으로 추후 삭제 가능)
    @PostMapping("/api/auth/kakao")
    public ResponseEntity<Map<String, String>> kakaoLogin(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 인가 코드 처리
        Map<String, String> tokens = oauthService.kakaoLogin(loginRequest.getCode(), request,
                response);
        return ResponseEntity.ok(tokens);
    }

    // 리다이렉트 URI에서 인가 코드를 자동으로 처리(운영용)
    @PostMapping("/api/auth/kakao")
    public ResponseEntity<Map<String, String>> handleKakaoLogin(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request,
            HttpServletResponse response) {
        String code = requestBody.get("code");

        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authorization code is missing"));
        }

        Map<String, String> tokens = oauthService.kakaoLogin(code, request, response);
        return ResponseEntity.ok(tokens);
    }

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;
}
