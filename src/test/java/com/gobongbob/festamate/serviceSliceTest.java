package com.gobongbob.festamate;

import com.gobongbob.festamate.common.builder.TestFixtureBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public abstract class serviceSliceTest {

    @Autowired
    protected TestFixtureBuilder testFixtureBuilder;
}
