//package com.middleware.edge.repository;
//
//import com.middleware.edge.model.EdgeCache;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.lang.NonNull;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//@Component
//@RequiredArgsConstructor
//public class RedisEdgeCacheRepository implements EdgeCacheRepository {
//
//  // 키 접두사 상수 정의 - edge2kIp가 이제 ID이므로 기본 키로 사용됩니다
//  private static final String KEY_PREFIX = "com.middleware.edge.model.EdgeCache:";
//  private static final String SPOT_CAMERA_INDEX = "spotCameraId:";
//  private static final String EDGE_4K_INDEX = "edge4kIp:";
//  private static final String SPOT_INTERSECTION_INDEX = "spotIntersectionId:";
//
//  private final RedisTemplate<String, EdgeCache> redisTemplate;
//  private final StringRedisTemplate stringRedisTemplate;
//
//  // 주어진 edge2kIp에 대한 Redis 키 생성 (이제 이것이 메인 키)
//  private String createKey(String edge2kIp) {
//    return KEY_PREFIX + edge2kIp;
//  }
//
//  // 각 인덱스에 대한 키 생성 메서드
//  private String createSpotCameraIdKey(String spotCameraId) {
//    return KEY_PREFIX + SPOT_CAMERA_INDEX + spotCameraId;
//  }
//
//  private String createEdge4kIpKey(String edge4kIp) {
//    return KEY_PREFIX + EDGE_4K_INDEX + edge4kIp;
//  }
//
//  private String createSpotIntersectionIdKey(String spotIntersectionId) {
//    return KEY_PREFIX + SPOT_INTERSECTION_INDEX + spotIntersectionId;
//  }
//
//  @Override
//  public List<EdgeCache> findAllByCameraId(String cameraId) {
//    // spotCameraId로 인덱싱된 키에서 edge2kIp를 조회
//    String edge2kIp = stringRedisTemplate.opsForValue().get(createSpotCameraIdKey(cameraId));
//    if (edge2kIp == null) {
//      return Optional.empty();
//    }
//    // edge2kIp로 실제 EdgeCache 객체 조회
//    return Optional.ofNullable(redisTemplate.opsForValue().get(createKey(edge2kIp)));
//  }
//
//  @Override
//  public Optional<EdgeCache> findByEdge4kIp(String edge4kIp) {
//    String edge2kIp = stringRedisTemplate.opsForValue().get(createEdge4kIpKey(edge4kIp));
//    if (edge2kIp == null) {
//      return Optional.empty();
//    }
//    return Optional.ofNullable(redisTemplate.opsForValue().get(createKey(edge2kIp)));
//  }
//
//  @Override
//  public List<EdgeCache> findAllByIntersectionId(String spotIntersectionId) {
//    // 교차로 ID에 해당하는 모든 edge2kIp Set을 조회
//    Set<String> edge2kIps = stringRedisTemplate.opsForSet()
//        .members(createSpotIntersectionIdKey(spotIntersectionId));
//
//    if (CollectionUtils.isEmpty(edge2kIps)) {
//      return Collections.emptyList();
//    }
//
//    // 각 edge2kIp에 대한 EdgeCache 객체 조회
//    return edge2kIps.stream()
//        .map(this::createKey)
//        .map(key -> redisTemplate.opsForValue().get(key))
//        .filter(Objects::nonNull)
//        .collect(Collectors.toList());
//  }
//
//  @Override
//  @NonNull
//  public <S extends EdgeCache> S save(S entity) {
//    if (entity.getEdge2kIp() == null) {
//      throw new IllegalArgumentException("Entity and edge2kIp must not be null");
//    }
//
//    String key = createKey(entity.getEdge2kIp());
//
//    // 기존 데이터가 있다면 인덱스 삭제
//    EdgeCache existingEntity = redisTemplate.opsForValue().get(key);
//    if (existingEntity != null) {
//      deleteIndexes(existingEntity);
//    }
//
//    // 메인 객체 저장
//    redisTemplate.opsForValue().set(key, entity);
//
//    // 모든 인덱스 저장
//    saveIndexes(entity);
//
//    return entity;
//  }
//
//  // 인덱스 저장을 위한 보조 메서드
//  private void saveIndexes(EdgeCache entity) {
//    // spotCameraId 인덱스 저장
//    if (entity.getSpotCameraId() != null) {
//      stringRedisTemplate.opsForValue()
//          .set(createSpotCameraIdKey(entity.getSpotCameraId()), entity.getEdge2kIp());
//    }
//
//    // edge4kIp 인덱스 저장
//    if (entity.getEdge4kIp() != null) {
//      stringRedisTemplate.opsForValue()
//          .set(createEdge4kIpKey(entity.getEdge4kIp()), entity.getEdge2kIp());
//    }
//
//    // spotIntersectionId 인덱스 저장 (Set 사용)
//    if (entity.getSpotIntersectionId() != null) {
//      stringRedisTemplate.opsForSet()
//          .add(createSpotIntersectionIdKey(entity.getSpotIntersectionId()),
//              entity.getEdge2kIp());
//    }
//  }
//
//  // 인덱스 삭제를 위한 보조 메서드
//  private void deleteIndexes(EdgeCache entity) {
//    if (entity.getSpotCameraId() != null) {
//      redisTemplate.delete(createSpotCameraIdKey(entity.getSpotCameraId()));
//    }
//    if (entity.getEdge4kIp() != null) {
//      redisTemplate.delete(createEdge4kIpKey(entity.getEdge4kIp()));
//    }
//    if (entity.getSpotIntersectionId() != null) {
//      stringRedisTemplate.opsForSet()
//          .remove(createSpotIntersectionIdKey(entity.getSpotIntersectionId()),
//              entity.getEdge2kIp());
//    }
//  }
//
//  @Override
//  @NonNull
//  public <S extends EdgeCache> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
//    RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
//    redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//      for (EdgeCache cache: entities) {
////        connection.set(Objects.requireNonNull(stringSerializer.serialize(KEY_PREFIX + cache.getEdge2kIp())),
////            Objects.requireNonNull(valueSerializer.serialize(cache)));
//        byte[] key = Objects.requireNonNull(stringSerializer.serialize(createKey(cache.getEdge2kIp())));
//
//        // Hash로 저장 (필드별 저장)
//        connection.hSet(key, Objects.requireNonNull(stringSerializer.serialize("edge2kIp")),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getEdge2kIp())));
//        connection.hSet(key, Objects.requireNonNull(stringSerializer.serialize("edge4kIp")),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getEdge4kIp())));
//        connection.hSet(key, Objects.requireNonNull(stringSerializer.serialize("spotCameraId")),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getSpotCameraId())));
//        connection.hSet(key,
//            Objects.requireNonNull(stringSerializer.serialize("spotIntersectionId")),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getSpotIntersectionId())));
//        connection.hSet(key,
//            Objects.requireNonNull(stringSerializer.serialize("ecuInfoTransmissionYesOrNo")),
//            Objects.requireNonNull(
//                stringSerializer.serialize(cache.getEcuInfoTransmissionYesOrNo())));
//        connection.hSet(key,
//            Objects.requireNonNull(stringSerializer.serialize("installedLocationNumber")),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getInstalledLocationNumber())));
//
//
//        connection.set(
//            Objects.requireNonNull(stringSerializer.serialize(KEY_PREFIX + SPOT_CAMERA_INDEX + cache.getSpotCameraId())),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getEdge2kIp())));
//        connection.set(
//            Objects.requireNonNull(stringSerializer.serialize(KEY_PREFIX + EDGE_4K_INDEX + cache.getEdge4kIp())),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getEdge2kIp())));
//        connection.sAdd(
//            Objects.requireNonNull(stringSerializer.serialize(KEY_PREFIX + SPOT_INTERSECTION_INDEX + cache.getSpotIntersectionId())),
//            Objects.requireNonNull(stringSerializer.serialize(cache.getEdge2kIp())));
//      }
//      return null;
//    });
//    return entities;
//  }
//
//  @Override
//  @NonNull
//  public Optional<EdgeCache> findById(@NonNull String edge2kIp) {
//    return Optional.ofNullable(redisTemplate.opsForValue().get(createKey(edge2kIp)));
//  }
//
//  @Override
//  public boolean existsById(@NonNull String edge2kIp) {
//    return Boolean.TRUE.equals(redisTemplate.hasKey(createKey(edge2kIp)));
//  }
//
//  @Override
//  @NonNull
//  public Iterable<EdgeCache> findAll() {
//    Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
//    if (CollectionUtils.isEmpty(keys)) {
//      return Collections.emptyList();
//    }
//
//    return keys.stream()
//        .filter(key -> !key.contains(SPOT_CAMERA_INDEX) &&
//            !key.contains(EDGE_4K_INDEX) &&
//            !key.contains(SPOT_INTERSECTION_INDEX))
//        .map(key -> redisTemplate.opsForValue().get(key))
//        .filter(Objects::nonNull)
//        .collect(Collectors.toList());
//  }
//
//  @Override
//  @NonNull
//  public Iterable<EdgeCache> findAllById(Iterable<String> edge2kIps) {
//    return StreamSupport.stream(edge2kIps.spliterator(), false)
//        .map(this::findById)
//        .filter(Optional::isPresent)
//        .map(Optional::get)
//        .collect(Collectors.toList());
//  }
//
//  @Override
//  public long count() {
//    Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
//    if (CollectionUtils.isEmpty(keys)) {
//      return 0;
//    }
//
//    return keys.stream()
//        .filter(key -> !key.contains(SPOT_CAMERA_INDEX) &&
//            !key.contains(EDGE_4K_INDEX) &&
//            !key.contains(SPOT_INTERSECTION_INDEX))
//        .count();
//  }
//
//  @Override
//  public void deleteById(@NonNull String edge2kIp) {
//    EdgeCache entity = findById(edge2kIp).orElse(null);
//    if (entity != null) {
//      delete(entity);
//    }
//  }
//
//  @Override
//  public void delete(@NonNull EdgeCache entity) {
//    if (entity.getEdge2kIp() == null) {
//      return;
//    }
//
//    // 모든 인덱스 삭제
//    deleteIndexes(entity);
//
//    // 메인 객체 삭제
//    redisTemplate.delete(createKey(entity.getEdge2kIp()));
//  }
//
//  @Override
//  public void deleteAllById(Iterable<? extends String> edge2kIps) {
//    edge2kIps.forEach(this::deleteById);
//  }
//
//  @Override
//  public void deleteAll(Iterable<? extends EdgeCache> entities) {
//    entities.forEach(this::delete);
//  }
//
//  @Override
//  public void deleteAll() {
//    Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
//    if (!CollectionUtils.isEmpty(keys)) {
//      redisTemplate.delete(keys);
//    }
//  }
//}