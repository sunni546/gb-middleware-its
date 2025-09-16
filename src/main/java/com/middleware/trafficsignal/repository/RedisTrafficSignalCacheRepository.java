//package com.middleware.trafficsignal.repository;
//
//import com.middleware.trafficsignal.model.TrafficSignalCache;
//import java.util.Optional;
//import java.util.stream.StreamSupport;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.lang.NonNull;
//import org.springframework.stereotype.Component;
//
//@Component
//@Primary
//@RequiredArgsConstructor
//public class RedisTrafficSignalCacheRepository implements TrafficSignalCacheRepository {
//
//  private static final String KEY_PREFIX = "com.middleware.trafficsignal.model.TrafficSignalCache:";
//
//  private final RedisTemplate<String, TrafficSignalCache> redisTemplate;
//  private final StringRedisTemplate stringRedisTemplate;
//
//  private String createKey(String spotIntersectionId) {
//    return KEY_PREFIX + spotIntersectionId;
//  }
//
//  @Override
//  @NonNull
//  public <S extends TrafficSignalCache> S save(@NonNull S entity) {
//
//    TrafficSignalCache trafficSignalCache = redisTemplate.opsForValue()
//        .get(createKey(entity.getSpotIntersectionId()));
//
//
//    cache.put(entity.getSpotIntersectionId(), entity);
//    return entity;
//  }
//
//  @Override
//  @NonNull
//  public <S extends TrafficSignalCache> Iterable<S> saveAll(Iterable<S> entities) {
//    entities.forEach(this::save);
//    return entities;
//  }
//
//  @Override
//  @NonNull
//  public Optional<TrafficSignalCache> findById(@NonNull String s) {
//    return Optional.ofNullable(cache.get(s));
//  }
//
//  @Override
//  public boolean existsById(@NonNull String s) {
//    return cache.containsKey(s);
//  }
//
//  @Override
//  @NonNull
//  public Iterable<TrafficSignalCache> findAll() {
//    return cache.values();
//  }
//
//  @Override
//  @NonNull
//  public Iterable<TrafficSignalCache> findAllById(Iterable<String> strings) {
//    return StreamSupport.stream(strings.spliterator(), true)
//        .map(cache::get).toList();
//  }
//
//  @Override
//  public long count() {
//    return cache.size();
//  }
//
//  @Override
//  public void deleteById(@NonNull String s) {
//    cache.remove(s);
//  }
//
//  @Override
//  public void delete(@NonNull TrafficSignalCache entity) {
//    deleteById(entity.getSpotIntersectionId());
//  }
//
//  @Override
//  public void deleteAllById(Iterable<? extends String> strings) {
//    strings.forEach(this::deleteById);
//  }
//
//  @Override
//  public void deleteAll(Iterable<? extends TrafficSignalCache> entities) {
//    entities.forEach(this::delete);
//  }
//
//  @Override
//  public void deleteAll() {
//    cache.clear();
//  }
//}
