package com.gobongbob.festamate.global;

import org.springframework.transaction.annotation.Transactional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;


//AOP의 관점(Aspect) 역할을 한다고 선언, 특정 메서드 실행 전후에 로직을 삽입할 수 있다.
@Aspect
@Component
public class TransactionRoutingAspect {

    // @Transactional 어노테이션이 붙은 메서드에 대해 실행 전후의 로직을 정의
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object routeTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        // joinPoint는 현재 실행 중인 메서드 정보를 담고 있으며, 이를 통해 원래 메서드를 호출할 수 있다.

        // 현재 실행 중인 메서드의 시그니처(메서드 이름, 반환 타입, 매개변수 등)를 가져온다.
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 현재 실행 중인 메서드에 @Transactional 어노테이션을 가져온다.
        Transactional transactional = signature.getMethod().getAnnotation(Transactional.class);

        if (transactional != null && transactional.readOnly()) {
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
        } else {
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
        }

        try {
            return joinPoint.proceed();
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

    /**
     * @ReadOnly와 @Write는 리플리케이션 환경에서 읽기 작업과 쓰기 작업을 구분하여
     * 적절한 데이터베이스(Master 또는 Slave)로 라우팅하는 목적으로만 사용
     * */

    // @ReadOnly 어노테이션이 붙은 메서드에 대해 실행 전후의 로직을 정의
    @Around("@annotation(com.gobongbob.festamate.global.ReadOnly)")
    public Object routeReadOnly(ProceedingJoinPoint joinPoint) throws Throwable {

        // 현재 트랜잭션 컨텍스트를 읽기 전용으로 설정하여 Slave DB를 사용하도록 지정한다.
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
        try {
            // 원래 메서드를 실행한다.
            return joinPoint.proceed();
        } finally {
            // 트랜잭션 컨텍스트를 초기화한다.
            TransactionSynchronizationManager.clear();
        }
    }

    // @Write 어노테이션이 붙은 메서드에 대해 실행 전후의 로직을 정의
    @Around("@annotation(com.gobongbob.festamate.global.Write)")
    public Object routeWrite(ProceedingJoinPoint joinPoint) throws Throwable {

        // 현재 트랜잭션 컨텍스트를 읽기 전용이 아니도록 설정하여 Master DB를 사용하도록 지정한다.
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
        try {
            // 원래 메서드를 실행한다.
            return joinPoint.proceed();
        } finally {
            // 트랜잭션 컨텍스트를 초기화한다.
            TransactionSynchronizationManager.clear();
        }
    }
}