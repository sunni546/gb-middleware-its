package com.middleware.trafficsignal.repository;

import com.middleware.trafficsignal.model.TrafficSignalCache;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MemoryTrafficSignalCacheRepository implements TrafficSignalCacheRepository {

  private final Map<String, TrafficSignalCache> cacheByIntersectionId = new ConcurrentHashMap<>();

  @Override
  @NonNull
  public TrafficSignalCache save(@NonNull TrafficSignalCache cache) {
    cacheByIntersectionId.put(cache.spotIntersectionId(), cache);
    return cache;
  }

  @Override
  @NonNull
  public List<TrafficSignalCache> saveAll(Collection<TrafficSignalCache> caches) {
    caches.forEach(this::save);
    return caches.stream().toList();
  }

  @Override
  @NonNull
  public Optional<TrafficSignalCache> findByIntersectionId(@NonNull String intersectionId) {
    return Optional.ofNullable(cacheByIntersectionId.get(intersectionId));
  }

  @Override
  public void deleteAll() {
    cacheByIntersectionId.clear();
  }
}
