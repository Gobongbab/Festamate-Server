package com.gobongbob.festamate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // test 프로파일 활성화
class FestamateApplicationTests {

    @Test
    @Disabled
        // 테스트 코드 비활성화
    void contextLoads() {
    }

}
