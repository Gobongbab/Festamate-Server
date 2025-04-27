//package com.gobongbob.festamate.global.config;
//
//import com.gobongbob.festamate.global.ReplicationRoutingDataSource;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//// 리플리케이션을 위한 데이터 소스 설정
//@Configuration
//public class DataSourceConfig {
//
//    private static final String MASTER_DATASOURCE = "masterDataSource";
//    private static final String SLAVE_DATASOURCE = "slaveDataSource";
//
//    /**
//     * HikariCP를 사용하여 마스터와 슬레이브 데이터베이스에 대한 DataSource를 설정
//     * */
//    @Bean(MASTER_DATASOURCE) // 이 메소드가 반환하는 객체를 "masterDataSource"라는 이름의 빈으로 Spring 컨테이너에 등록
//    @ConfigurationProperties(prefix = "spring.datasource.master") // application 설정 파일에서 "spring.datasource.master" 로 시작하는 속성들을 읽어
//                                                                                // 이 DataSource 객체의 필드에 자동으로 바인딩(주입)합니다. (예: jdbc-url, username, password 등)
//    public DataSource masterDataSource() {
//        // DataSourceBuilder를 사용하여 DataSource 객체를 생성합니다.
//        // .type(HikariDataSource.class) : 성능 좋은 HikariCP 커넥션 풀 구현체를 사용하도록 지정합니다.
//        // .build() : 설정된 정보를 바탕으로 DataSource 객체를 최종 생성하여 반환합니다.
//        return DataSourceBuilder.create().type(HikariDataSource.class).build();
//    }
//
//    @Bean(SLAVE_DATASOURCE)
//    @ConfigurationProperties(prefix = "spring.datasource.slave")
//    public DataSource slaveDataSource() {
//        return DataSourceBuilder.create().type(HikariDataSource.class).build();
//    }
//
//    // 라우팅 기능을 담당하는 DataSource 빈을 생성하는 메소드
//    @Bean
//    public DataSource replicationRoutingDataSource(
//            // Spring 컨테이너에 등록된 빈 중에서 이름(Qualifier)이 "masterDataSource"인 DataSource 빈을 찾아 주입받습니다.
//            @Qualifier(MASTER_DATASOURCE) DataSource masterDataSource,
//            // Spring 컨테이너에 등록된 빈 중에서 이름(Qualifier)이 "replicaDataSource"인 DataSource 빈을 찾아 주입받습니다.
//            @Qualifier(SLAVE_DATASOURCE) DataSource replicaDataSource) {
//
//        // AbstractRoutingDataSource를 상속받은 사용자 정의 ReplicationRoutingDataSource 객체를 생성합니다.
//        ReplicationRoutingDataSource replicationRoutingDataSource = new ReplicationRoutingDataSource();
//
//        // 실제 사용할 DataSource들을 담을 Map 객체를 생성합니다. Key는 라우팅 구분자(문자열), Value는 DataSource 객체입니다.
//        Map<Object, Object> targetDataSources = new HashMap<>();
//        // "master"라는 키에 masterDataSource 빈을 매핑합니다.
//        targetDataSources.put("master", masterDataSource);
//        // "slave"라는 키에 replicaDataSource 빈을 매핑합니다.
//        targetDataSources.put("slave", replicaDataSource);
//
//        // 생성한 targetDataSources 맵을 replicationRoutingDataSource 객체에 설정합니다. replicationRoutingDataSource는 이 맵을 보고 실제 사용할 DataSource를 결정합니다.
//        replicationRoutingDataSource.setTargetDataSources(targetDataSources);
//        // 어떤 DataSource를 사용할지 결정할 수 없을 때(기본값) 사용할 DataSource를 masterDataSource로 설정합니다.
//        replicationRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
//
//        // 설정이 완료된 replicationRoutingDataSource 객체를 반환합니다. 이 빈은 아직 @Primary가 아니므로 직접 주입 시 사용됩니다.
//        return replicationRoutingDataSource;
//    }
//
//    @Primary
//    @Bean
//    public DataSource dataSource(
//            @Qualifier("replicationRoutingDataSource") DataSource replicationRoutingDataSource) {
//        // LazyConnectionDataSourceProxy 객체를 생성하여 replicationRoutingDataSource를 감쌉니다.
//        // 중요: 이는 실제 DB 커넥션이 필요한 시점(보통 SQL 실행 직전)까지 커넥션 획득을 지연시킵니다.
//        // 이 지연 덕분에 트랜잭션이 시작되고 @Transactional의 readOnly 속성이 적용된 이후에
//        // replicationRoutingDataSource의 determineCurrentLookupKey() 메소드가 호출되어 올바른 DataSource(Master 또는 Replica)가 선택될 수 있습니다.
//        return new LazyConnectionDataSourceProxy(replicationRoutingDataSource);
//    }
//}