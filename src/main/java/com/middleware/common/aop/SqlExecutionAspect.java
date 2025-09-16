package com.middleware.common.aop;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class SqlExecutionAspect {

  @Around("@annotation(SqlRetryOnFailure)")
  public Object retryOnFailure(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    SqlRetryOnFailure annotation = method.getAnnotation(SqlRetryOnFailure.class);

    final int maxRetryAttempts = annotation.retryAttempts();
    for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
      try {
        return joinPoint.proceed();
      } catch (DataAccessResourceFailureException e) {
        log.warn("SQL execution failed. Attempt {}/{}", attempt, maxRetryAttempts);
        if (attempt == maxRetryAttempts) {
          log.error("SQL execution failed. Attempt {}/{}", attempt, maxRetryAttempts, e);
          throw e;
        }
      }
    }
    return null;
  }
}
