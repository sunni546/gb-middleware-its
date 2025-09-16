package com.middleware.edge.generated.service;

import com.middleware.edge.generated.config.DB;
import com.middleware.edge.generated.config.DataSaveConfig;
import com.middleware.edge.generated.config.TableType;
import com.middleware.edge.generated.repository.QueryExecutor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
@ConditionalOnProperty(value = "edge.generated.enabled", havingValue = "true", matchIfMissing = false)
public class EdgeDataQueueManager {

    private final Map<Class<?>, WaitingQueueContainer<?>> waitingQueueMap;
    private final Map<Class<?>, Map<QueryExecutor, String>> sqlMap;
    private final Map<Class<?>, ReentrantLock> drainLocks; // 클래스별 드레인 락 추가
    private final ScheduledExecutorService scheduledDrainExecutor; // 주기적 드레인을 위한 스케줄러 추가

    public EdgeDataQueueManager(DataSaveConfig dataSaveConfig, List<QueryExecutor> queryExecutors) {
        Map<DB, QueryExecutor> dbToExecutor = queryExecutors.stream()
            .collect(Collectors.toMap(QueryExecutor::getDb, Function.identity()));

        // 2. 전략별 큐와 SQL Map 초기화
        Map<Class<?>, WaitingQueueContainer<?>> tempWaitingQueueMap = new HashMap<>();
        Map<Class<?>, Map<QueryExecutor, String>> tempSqlMap = new HashMap<>();
        Map<Class<?>, ReentrantLock> tempDrainLocks = new ConcurrentHashMap<>();


        dataSaveConfig.saveStrategies().forEach(saveStrategy -> {

            tempWaitingQueueMap.put(saveStrategy.clazz(), WaitingQueueContainer.builder()
                .tableType(TableType.fromClass(saveStrategy.clazz()))
                .drainMaxMillis(saveStrategy.drainMaxMillis())
                .maxSize(saveStrategy.maxSize())
                .graceDelayMillis(300L) // 모든 테이블에 동일한 300ms Grace 기간 적용
                .lastDrainMilli(new AtomicLong(System.currentTimeMillis()))
                .lastEnqueueMilli(new AtomicLong(System.currentTimeMillis())) // 마지막 입력 시간 초기화
                .build()
            );

            // 클래스별 드레인 락 생성
            tempDrainLocks.put(saveStrategy.clazz(), new ReentrantLock());

            tempSqlMap.put(saveStrategy.clazz(), saveStrategy.queries().stream()
                .collect(Collectors.toMap(
                    sql -> {
                        QueryExecutor executor = dbToExecutor.get(sql.db());
                        if (executor == null) {
                            throw new IllegalStateException(
                                "No QueryExecutor found for DB: " + sql.db());
                        }
                        return executor;
                    },
                    DataSaveConfig.EdgeDataSaveStrategy.SqlPerDb::sql
                )));
        });
        this.waitingQueueMap = Collections.unmodifiableMap(tempWaitingQueueMap);
        this.sqlMap = Collections.unmodifiableMap(tempSqlMap);
        this.drainLocks = tempDrainLocks;
        this.scheduledDrainExecutor = Executors.newScheduledThreadPool(1);
        
        // 주기적 드레인 스케줄링 시작
        startPeriodicDrain();
    }

    @SuppressWarnings("unchecked")
    public <T> void enqueueAndDrainIfNeeded(final T data) {
        Class<T> clazz = (Class<T>) data.getClass();
        WaitingQueueContainer<T> container = (WaitingQueueContainer<T>) waitingQueueMap.get(clazz);
        if (container == null) {
            throw new IllegalStateException("No queue configured for class: " + clazz.getName());
        }
        container.enqueue(data);
        
        // 동시성을 고려한 드레인 처리
        if (container.shouldDrain()) {
            tryDrainWithLock(clazz, container);
        }
    }

    // AOP로 drain 간격 및 크기 메트릭 전송을 위해 분리
    public <T> List<T> drain(WaitingQueueContainer<T> container) {
        final List<T> drain = container.drain();
        if (drain == null || drain.isEmpty()) return List.of();
        return drain;
    }

    public void tryBatchInsertOrSingle(QueryExecutor queryExecutor, final String sql, final List<Object> data) {
        // 에러 추적을 위한 로깅용 식별자
        String batchId = UUID.randomUUID().toString().substring(0, 8);
        try {
            queryExecutor.executeBatch(sql, data);
        } catch (Exception e) {
            log.error("batch-id[{}] Error while <{}> batch query execute {} rows.", batchId, queryExecutor.getDb().getValue(), data.size(), e);
            data.forEach(singleData -> {
                // batch insert 중 예외 발생시 단일 쿼리 실행
                try {
                    queryExecutor.executeSingle(sql, singleData);
                } catch (Exception e1) {
                    log.error("batch-id[{}] Error while <{}> single query execution after batch execution. record : {}",
                        batchId, queryExecutor.getDb().getValue(), singleData, e1);
                }
            });
        }
    }

    private <T> void tryDrainWithLock(Class<T> clazz, WaitingQueueContainer<T> container) {
        ReentrantLock lock = drainLocks.get(clazz);
        if (lock.tryLock()) {
            try {
                performDrain(container);
            } finally {
                lock.unlock();
            }
        } else {
            log.trace("Drain already in progress for {}", clazz.getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void performDrain(WaitingQueueContainer<T> container) {
        List<T> drainedData = drain(container);
        if (!drainedData.isEmpty()) {
            Map<QueryExecutor, String> executorSqlMap = sqlMap.get(container.getTableType().getClazz());
            if (executorSqlMap == null) {
                throw new IllegalStateException("No SQL configuration found for class: " + container.getTableType().getClazz().getName());
            }
            
            executorSqlMap.forEach(
                (executor, sql) -> tryBatchInsertOrSingle(executor, sql, (List<Object>) drainedData)
            );
        }
    }

    /**
     * 주기적으로 모든 큐를 체크하여 드레인이 필요한 것들을 처리
     */
    private void startPeriodicDrain() {
        scheduledDrainExecutor.scheduleWithFixedDelay(() -> {
            waitingQueueMap.forEach((clazz, container) -> {
                if (container.getCurrentSize() > 0 && container.shouldDrain()) {
                    tryDrainWithLockRaw(clazz, container);
                }
            });
        }, 500, 500, TimeUnit.MILLISECONDS); // 500ms마다 체크
    }

    @SuppressWarnings("unchecked")
    private void tryDrainWithLockRaw(Class<?> clazz, WaitingQueueContainer<?> container) {
        ReentrantLock lock = drainLocks.get(clazz);
        if (lock.tryLock()) {
            try {
                performDrainRaw(container);
            } finally {
                lock.unlock();
            }
        } else {
            log.trace("Drain already in progress for {}", clazz.getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    private void performDrainRaw(WaitingQueueContainer<?> container) {
        List<?> drainedData = container.drain();
        if (!drainedData.isEmpty()) {
            Map<QueryExecutor, String> executorSqlMap = sqlMap.get(container.getTableType().getClazz());
            if (executorSqlMap == null) {
                throw new IllegalStateException("No SQL configuration found for class: " + container.getTableType().getClazz().getName());
            }
            
            executorSqlMap.forEach(
                (executor, sql) -> tryBatchInsertOrSingle(executor, sql, (List<Object>) drainedData)
            );
        }
    }
} 