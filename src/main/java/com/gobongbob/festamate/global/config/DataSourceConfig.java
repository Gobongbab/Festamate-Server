package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.global.ReplicationRoutingDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// 리플리케이션을 위한 데이터 소스 설정
@Configuration
public class DataSourceConfig {

    /**
     * 각각 마스터와 슬레이브 데이터베이스의 연결 설정을 정의
     * */
    // 마스터 데이터 소스 프로퍼티를 설정하는 빈 정의
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    // 슬레이브 데이터 소스 프로퍼티를 설정하는 빈 정의
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSourceProperties slaveDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 위에서 로드한 프로퍼티를 사용하여 각각 실제 데이터 소스를 초기화
     * */
    // 마스터 데이터 소스를 초기화하는 빈 정의
    @Bean
    public DataSource masterDataSource() {
        return masterDataSourceProperties().initializeDataSourceBuilder().build();
    }

    // 슬레이브 데이터 소스를 초기화하는 빈 정의
    @Bean
    public DataSource slaveDataSource() {
        return slaveDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * 마스터와 슬레이브 데이터 소스를 라우팅하는 데이터 소스를 설정
     * */
    // 라우팅 데이터 소스를 설정하는 빈 정의
    @Bean
    public DataSource routingDataSource() {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();

        // 마스터와 슬레이브 데이터 소스를 맵에 추가
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("slave", slaveDataSource());

        // 라우팅 데이터 소스에 타겟 데이터 소스 맵 설정
        routingDataSource.setTargetDataSources(dataSourceMap);
        // 기본 데이터 소스를 마스터로 설정
        routingDataSource.setDefaultTargetDataSource(masterDataSource());

        return routingDataSource;
    }

    /**
     * LazyConnectionDataSourceProxy를 사용하여 실제 데이터 소스를 런타임에 결정
     * */
    // 기본 데이터 소스를 설정하는 빈 정의
    @Primary
    @Bean
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }
}