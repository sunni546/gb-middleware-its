package com.middleware.trafficsignal;

import static java.util.stream.Collectors.collectingAndThen;

import com.middleware.grpc.exceptions.GrpcEdgeNotExistException;
import com.middleware.trafficsignal.event.TrafficSignalChangeEvent;
import com.middleware.trafficsignal.model.TrafficSignalCache;
import com.middleware.trafficsignal.model.TrafficSignalDto;
import com.middleware.trafficsignal.repository.TrafficSignalCacheRepository;
import com.middleware.trafficsignal.repository.TrafficSignalRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "traffic-signal.enabled", havingValue = "true", matchIfMissing = false)
public class TrafficSignalService {
  private final TrafficSignalRepository signalRepository;
  private final TrafficSignalCacheRepository signalCacheRepository;
  private final ApplicationEventPublisher eventPublisher;

  @PostConstruct
  public void init() {
    signalCacheRepository.saveAll(getAllCacheFromDb());
  }

  // TODO 신호 업데이트 주기 외부에서 설정,
  //  현재는 약 3000개의 전체 신호 교차로의 신호정보 조회 중,
  //  SPOT_INTS_INFO 테이블에 있는 200개의 정보만 IN 쿼리로 지정해서 조회해 최적화 희망
  //  DB 별로 IN 쿼리 갯수 제한이 있어 Batch 크기 조정해서 나눠서 해볼만도...
  @Scheduled(fixedDelay = 1000)
  public void updateAllTrafficSignalsAndNotifyChange() {
    List<TrafficSignalDto> updatedSignals = getAllCacheFromDb()
        .stream()
        .filter(this::isSignalChanged)
        .collect(collectingAndThen(
            Collectors.toList(), signalCacheRepository::saveAll))
        .stream()
        .map(TrafficSignalDto::from)
        .toList();

    eventPublisher.publishEvent(new TrafficSignalChangeEvent(updatedSignals));
  }


  // 요청에 의한 신호 전달을 위한 단건 조회(gRPC에서는 연결시, Redis에서는 Edge 요청시)
  public TrafficSignalDto getDtoByIntersectionId(String spotIntersectionId) {
    return TrafficSignalDto.from(
        getCacheByIntersectionIdFromCache(spotIntersectionId));
  }

  private boolean isSignalChanged(TrafficSignalCache newSignal) {
    return !newSignal.equals(getCacheByIntersectionIdFromCache(newSignal.spotIntersectionId()));
  }

  private TrafficSignalCache getCacheByIntersectionIdFromCache(String spotIntersectionId) {
    return signalCacheRepository.findByIntersectionId(spotIntersectionId)
        .orElseThrow(() -> new GrpcEdgeNotExistException(spotIntersectionId));
  }

  private List<TrafficSignalCache> getAllCacheFromDb() {
    return signalRepository.findAllTrafficSignals().stream()
        .map(TrafficSignalCache::from).toList();
  }
}
