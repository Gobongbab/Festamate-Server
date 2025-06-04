package com.gobongbob.festamate.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    // 현재 사용할 DataSource의 키(lookup key)를 결정하는 핵심 메소드. Spring의 트랜잭션 관리자가 호출합니다.
    @Override
    protected Object determineCurrentLookupKey() {
//        // TransactionSynchronizationManager를 사용하여 현재 실행 중인 스레드에 활성화된 트랜잭션이 읽기 전용(read-only)인지 확인합니다.
//        // @Transactional(readOnly = true) 가 적용된 서비스 메소드가 호출되면 true가 됩니다.
//        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
//
//        // 만약 현재 트랜잭션이 읽기 전용이라면
//        if (isReadOnly) {
//            // "slave" 라는 문자열(lookup key)을 반환합니다.
//            // AbstractRoutingDataSource는 이 키를 사용하여 targetDataSources 맵에서 실제 사용할 DataSource (replicaDataSource)를 찾습니다.
//            log.info("Routing to SLAVE data source");
//            return "slave";
//        } else {
//            log.info("Routing to MASTER data source");
//            return "master";
//        }
        return "master"; // 기본적으로 master로 설정하여 리플리케이션 비활성화
    }
}