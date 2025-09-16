package com.middleware.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MethodExecutionTimerAspect {

  @Around("@annotation(ExecutionTimer)")
  public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    // 시작 시간 기록
    long startTime = System.currentTimeMillis();

    // 메서드 실행
    Object result = joinPoint.proceed();

    // 종료 시간 기록
    long endTime = System.currentTimeMillis();

    // 클래스와 메서드 이름 얻기
    String className = joinPoint.getSignature().getDeclaringTypeName();
    String methodName = joinPoint.getSignature().getName();

    // 실행 시간 계산
    long executionTime = endTime - startTime;

    // 로그 출력
    log.info("{}/{}(): Execution Time: {} ms", className, methodName, executionTime);
    return result;
  }
}
