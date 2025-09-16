package com.middleware.edge.selfinfo;

import com.middleware.common.aop.ExecutionTimer;
import com.middleware.common.exceptions.NotFoundException;
import com.middleware.edge.selfinfo.config.LaneOffsetOverrideProperties;
import com.middleware.edge.selfinfo.model.CameraDto;
import com.middleware.edge.selfinfo.model.EdgeCache;
import com.middleware.edge.selfinfo.model.EdgeInfo;
import com.middleware.edge.selfinfo.repository.EdgeCacheRepository;
import com.middleware.edge.selfinfo.repository.CameraRepository;
import com.middleware.edge.selfinfo.repository.LaneRepository;
import com.middleware.grpc.exceptions.GrpcEdgeNotExistException;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "edge.self-info.enabled", havingValue = "true", matchIfMissing = false)
public class EdgeService {

  private final CameraRepository cameraRepository;
  private final EdgeCacheRepository edgeCacheRepository;
  private final LaneRepository laneRepository;
  private final LaneOffsetOverrideProperties overrideProps;

  public String getCameraIdByIp(String ip) {
    return getEdgeByIp(ip).spotCameraId();
  }

  public int getLaneOffsetByIp(String ip) {
    return getEdgeByIp(ip).laneOffset();
  }

  public EdgeInfo getEdgeInfoByIp(String ip) {
    return EdgeInfo.from(getEdgeByIp(ip));
  }

  public Set<EdgeCache> getEdgesInSpot(String ip) {
    String intersectionId = getIntersectionIdByIp(ip);
    return new HashSet<>(getAllEdgesByIntersectionId(intersectionId));
  }

  public String getIntersectionIdByIp(String ip) {
    return getEdgeByIp(ip).spotIntersectionId();
  }

  private EdgeCache getEdgeByIp(String ip) {
    Supplier<Optional<EdgeCache>> getByIp = () -> edgeCacheRepository.findByIp(ip);
    try {
      return findByFunctionFromCacheOrDb(getByIp, Optional::isEmpty, Optional::get);
    } catch (NotFoundException e) {
      log.error("Edge not found for ip address : {}", ip, e);
      throw new GrpcEdgeNotExistException("Edge not found for ip address", e);
    }
  }

  private Collection<EdgeCache> getAllEdgesByIntersectionId(String intersectionId) {
    Supplier<Collection<EdgeCache>> getByIntersectionId = () -> edgeCacheRepository.findAllByIntersectionId(intersectionId);
    try {
      return findByFunctionFromCacheOrDb(getByIntersectionId, Collection::isEmpty, Function.identity());
    } catch (NotFoundException e) {
      log.error("Edge not found for intersectionId : {}", intersectionId, e);
      throw new GrpcEdgeNotExistException("Edge not found for intersectionId", e);
    }
  }

//  추상화 없이 작성된 메서드
//  private EdgeCache getEdgeByCameraId(String spotCameraId) {
//    return edgeRedisRepository.findById(spotCameraId).or(() -> {
//      initializeCaches();
//      return edgeRedisRepository.findById(spotCameraId);
//    }).orElseThrow(() -> new EdgeNotExistException("No edge found from Redis and Db for spotCameraId : " + spotCameraId));
//  }
  @ExecutionTimer
  protected <T, R> R findByFunctionFromCacheOrDb(Supplier<T> findFromCache, Predicate<T> isEmpty, Function<T, R> converter) {
    return Optional.ofNullable(findFromCache.get()) // 최초 캐시 조회해서 Optional로 래핑
        .filter(isEmpty.negate()) // 조회 결과 부재시 Optional.empty()로 초기화
        .or(() -> { // Optional.empty()이면
          initializeCaches(); // 캐시 지우고 DB에서 다시 캐싱
          return Optional.ofNullable(findFromCache.get()); // 캐시에서 다시 조회해서 Optional로 래핑
        })
        .filter(isEmpty.negate()) // 조회 결과 부재시 Optional.empty()로 초기화
        .map(converter) // 값 변환(ex. Optional 이면 .get(), List()이면 그대로)
        .orElseThrow(() -> new NotFoundException("No edge found from Redis and Db")); // 마지막 까지 조회 결과가 없으면 예외 발생
  }

  @PostConstruct
  public void init() {
    initializeCaches();
  }

  @ExecutionTimer
  public void initializeCaches() {
    edgeCacheRepository.deleteAll();

    List<CameraDto> cameras = cameraRepository.findAllCameraInfo();

    // 4K 카메라 ID만 모으기
    Set<String> ids4k = cameras.stream()
        .filter(dto -> dto.edge4kIp() != null && dto.spotCameraId() != null)
        .map(CameraDto::spotCameraId)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    // 벌크 최소 차로번호 조회
    Map<String, Integer> minLaneMap = laneRepository.findMinLaneNoByCameraIds(ids4k);

    // 오버라이드 설정 가져오기
    Map<String, Integer> overrideByIp = overrideProps.getIp();

    List<EdgeCache> edgeCaches = cameras.stream()
        .flatMap(edgeDto -> EdgeCache.from(edgeDto, minLaneMap, overrideByIp).stream())
        .toList();

    edgeCacheRepository.saveAll(edgeCaches);
  }
}
