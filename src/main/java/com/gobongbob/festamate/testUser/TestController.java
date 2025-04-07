package com.gobongbob.festamate.testUser;

import com.gobongbob.festamate.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/create")
    public SuccessResponse<TestService.TestTokens> createTestMember() {
        // 테스트용 유저 생성 및 JWT 토큰 반환
        TestService.TestTokens testTokens = testService.createTestMember();
        return new SuccessResponse<>(testTokens);
    }
}
