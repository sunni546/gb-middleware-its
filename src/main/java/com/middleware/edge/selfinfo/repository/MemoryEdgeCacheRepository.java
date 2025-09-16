package com.middleware.edge.selfinfo.repository;

import com.middleware.edge.selfinfo.model.EdgeCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
public class MemoryEdgeCacheRepository implements EdgeCacheRepository {

  Map<String, EdgeCache> cacheByIp = new ConcurrentHashMap<>();
  Map<String, Set<EdgeCache>> cacheByCameraId = new ConcurrentHashMap<>();
  Map<String, Set<EdgeCache>> cacheByIntersectionId = new ConcurrentHashMap<>();

  @Override
  @NonNull
  public Optional<EdgeCache> findByIp(@NonNull String ip) {
    return Optional.ofNullable(cacheByIp.get(ip));
  }


  @Override
  public List<EdgeCache> findAllByIntersectionId(String spotIntersectionId) {
    return Optional.ofNullable(cacheByIntersectionId.get(spotIntersectionId))
        .map(ArrayList::new)
        .orElse(new ArrayList<>());
  }

  @Override
  @NonNull
  public EdgeCache save(@NonNull EdgeCache cache) {
    log.info("Saving edge cache: {}", cache);
    log.info("cache map: {}",cacheByIp);
    cacheByIp.put(cache.ip(), cache);

    BiFunction<String, Set<EdgeCache>, Set<EdgeCache>> mappingFunction =
        (key, existingSet) ->  {
      if (existingSet == null) {
        Set<EdgeCache> newSet = ConcurrentHashMap.newKeySet();
        newSet.add(cache);
        return newSet;
      }
      existingSet.add(cache);
      return existingSet;
    };
    cacheByIntersectionId.compute(
        cache.spotIntersectionId(), mappingFunction);
    cacheByCameraId.compute(
        cache.spotCameraId(), mappingFunction);
    return cache;
  }

  @Override
  @NonNull
  public List<EdgeCache> saveAll(Collection<EdgeCache> caches) {
    log.info("Saving edges to cache: {}", caches);
    caches.forEach(this::save);
    return caches.stream().toList();
  }


  @Override
  public void deleteAll() {
    cacheByCameraId.clear();
    cacheByIntersectionId.clear();
    cacheByIp.clear();
  }
}
