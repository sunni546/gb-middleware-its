package com.middleware.trafficsignal.repository;

import com.middleware.trafficsignal.model.TrafficSignalCache;
import java.util.Collection;
import java.util.Optional;

public interface TrafficSignalCacheRepository {

  TrafficSignalCache save(TrafficSignalCache trafficSignalCache);

  Collection<TrafficSignalCache> saveAll(Collection<TrafficSignalCache> trafficSignalCaches);

  Optional<TrafficSignalCache> findByIntersectionId(String intersectionId);

  void deleteAll();
}
