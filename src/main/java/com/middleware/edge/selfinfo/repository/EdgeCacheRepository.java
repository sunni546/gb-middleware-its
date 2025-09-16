package com.middleware.edge.selfinfo.repository;

import com.middleware.edge.selfinfo.model.EdgeCache;
import java.util.Collection;
import java.util.Optional;

public interface EdgeCacheRepository  {

  EdgeCache save(EdgeCache cache);

  Collection<EdgeCache> saveAll(Collection<EdgeCache> caches);

  void deleteAll();

  Optional<EdgeCache> findByIp(String ip);

  Collection<EdgeCache> findAllByIntersectionId(String spotIntersectionId);
}
