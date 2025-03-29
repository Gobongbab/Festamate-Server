package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.global.TransactionRoutingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


// 트랜잭션 관리를 활성화하는 설정 클래스
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    // 트랜잭션 매니저 빈 정의
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // 트랜잭션 라우팅을 위한 Aspect 빈 정의
    @Bean
    public TransactionRoutingAspect transactionRoutingAspect() {
        return new TransactionRoutingAspect();
    }
}