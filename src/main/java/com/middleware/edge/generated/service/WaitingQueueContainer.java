package com.middleware.edge.generated.service;

import com.middleware.edge.generated.config.TableType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;
import lombok.Getter;

@Builder
public class WaitingQueueContainer<T> {
  @Getter
  private final TableType tableType;
  private final int maxSize;
  private final long drainMaxMillis;
  private final long graceDelayMillis; // 추가: 동시 요청 수집을 위한 대기 시간
  private AtomicLong lastDrainMilli;
  private AtomicLong lastEnqueueMilli; // 추가: 마지막 입력 시간 추적
  private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

  public List<T> drain() {
    ArrayList<T> drained = new ArrayList<>();
    queue.drainTo(drained, maxSize);
    updateLastDrainMilli();
    return drained;
  }

  public boolean shouldDrain() {
    // 1. 최대 크기 도달 시 즉시 드레인
    if (exceededMaxSizeAndNotEmpty()) {
      return true;
    }
    
    // 2. 시간 제한 초과 시 드레인
    if (exceededDrainTimeLimit()) {
      return true;
    }
    
    // 3. Grace 기간을 고려한 배치 최적화
    // 마지막 입력 후 일정 시간이 지났고, 큐에 데이터가 있으면 드레인
    if (hasGracePeriodExpired() && !queue.isEmpty()) {
      return true;
    }
    
    return false;
  }

  public void enqueue(T data) {
    queue.add(data);
    updateLastEnqueueMilli(); // 마지막 입력 시간 업데이트
  }

  public long getLastDrainMilli() {
    return lastDrainMilli.get();
  }

  private void updateLastDrainMilli() {
    lastDrainMilli.set(System.currentTimeMillis());
  }

  private boolean exceededDrainTimeLimit() {
    return getRemainingTime() >= drainMaxMillis;
  }

  private boolean exceededMaxSizeAndNotEmpty() {
    return queue.size() >= maxSize && !queue.isEmpty();
  }

  private long getRemainingTime() {
    return System.currentTimeMillis() - lastDrainMilli.get();
  }

  // 추가 메서드들
  private void updateLastEnqueueMilli() {
    if (lastEnqueueMilli != null) {
      lastEnqueueMilli.set(System.currentTimeMillis());
    }
  }

  private boolean hasGracePeriodExpired() {
    if (lastEnqueueMilli == null || graceDelayMillis <= 0) {
      return false;
    }
    return (System.currentTimeMillis() - lastEnqueueMilli.get()) >= graceDelayMillis;
  }

  public int getCurrentSize() {
    return queue.size();
  }

}
