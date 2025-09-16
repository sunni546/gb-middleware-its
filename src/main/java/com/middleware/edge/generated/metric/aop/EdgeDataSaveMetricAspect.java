package com.middleware.edge.generated.metric.aop;

import com.middleware.edge.generated.config.DB;
import com.middleware.edge.generated.config.TableType;
import com.middleware.edge.generated.repository.QueryExecutor;
import com.middleware.edge.generated.service.WaitingQueueContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class EdgeDataSaveMetricAspect {

  private final MeterRegistry meterRegistry;

  // QueryExecutor를 상속하는 모든 클래스의 execute로 시작하는 메서드에 적용
  @Around(
      "execution(* com.middleware.edge.generated.repository.QueryExecutor+.executeBatch(..)) || " +
          "execution(* com.middleware.edge.generated.repository.QueryExecutor+.executeSingle(..))"
  )  public Object measureQueryExecution(ProceedingJoinPoint pjp) throws Throwable {
    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    String methodName = method.getName();
    Object[] args = pjp.getArgs();

    long start = System.currentTimeMillis();
    Object result = pjp.proceed();  // 메서드 실행
    long end = System.currentTimeMillis();

    long executionMillis = end - start;
    Class<?> dataClass;
    if (methodName.equals("executeBatch")) {
      List<?> items = (List<?>) args[1];
      if (items.isEmpty()) {
        return result; // 빈 리스트인 경우 메트릭 기록하지 않고 반환
      }
      dataClass = items.getFirst().getClass();
    } else { // executeSingle
      dataClass = args[1].getClass();
    }
    TableType tableType = TableType.fromClass(dataClass);

    // QueryExecutor를 상속한 실제 인스턴스에서 DB enum 추출
    Object target = pjp.getTarget();
    DB db = null;
    if (target instanceof QueryExecutor executor) {
      db = executor.getDb();
    }

    // 메트릭 기록
    String[] tags = new String[] {
        "table", tableType.name(),
        "db", db.name()
    };

    // 타이머 기록
    meterRegistry.timer("edge.insert.execution.time", tags)
        .record(Duration.ofMillis(executionMillis));

    // 카운터 기록: insert 처리된 행 수
    int count = methodName.equals("executeBatch") ? ((List<?>) args[1]).size() : 1;
    meterRegistry.counter("edge.insert.record.count", tags)
        .increment(count);

    return result;
  }

  // 특정 테이블의 큐가 비워질 때 = EdgeDataQueueManager.drain 메서드 실행될때 적용
  @Around("execution(* com.middleware.edge.generated.service.EdgeDataQueueManager.drain(..))")
  public Object recordDrainSizeAndInterval(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();
    WaitingQueueContainer<?> container = (WaitingQueueContainer<?>) args[0];

    TableType tableType = container.getTableType();
    List<Tag> tags = List.of(Tag.of("table", tableType.name()));

    long now = System.currentTimeMillis();
    long sinceLastDrain = now - container.getLastDrainMilli();

    Object result = pjp.proceed(); // List<T> 반환
    int drainedSize = ((List<?>) result).size();

    meterRegistry.counter("edge.drain.count", tags).increment(drainedSize);
    meterRegistry.gauge("edge.drain.interval.millis", tags, sinceLastDrain);

    return result;
  }
}