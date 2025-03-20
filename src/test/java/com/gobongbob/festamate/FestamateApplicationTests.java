package com.gobongbob.festamate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FestamateApplication.class)
@ActiveProfiles("test") // test 프로파일 활성화
class FestamateApplicationTests {

    @Test
    void contextLoads() {
    }

}
