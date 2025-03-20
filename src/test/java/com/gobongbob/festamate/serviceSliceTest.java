package com.gobongbob.festamate;

import com.gobongbob.festamate.common.builder.TestFixtureBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = FestamateApplication.class) // Spring Context 로드
@ActiveProfiles("test") // 테스트 환경 설정 적용
@Transactional
public abstract class serviceSliceTest {

    @Autowired
    protected TestFixtureBuilder testFixtureBuilder;
}
