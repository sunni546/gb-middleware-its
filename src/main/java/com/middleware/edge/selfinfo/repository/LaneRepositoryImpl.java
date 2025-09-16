package com.middleware.edge.selfinfo.repository;

import com.middleware.common.config.JdbcTemplateConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LaneRepositoryImpl implements LaneRepository {
  private final JdbcTemplate jdbcTemplate;

  public LaneRepositoryImpl(@Qualifier(JdbcTemplateConfig.VOLT_DB_DEFAULT) JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Integer> findMinLaneNoByCameraIds(Set<String> cameraIds) {
    if (cameraIds == null || cameraIds.isEmpty()) return Collections.emptyMap();

    final int CHUNK = 500; // Volt/JDBC 안정권. 필요시 조정
    Map<String, Integer> out = new HashMap<>();
    List<String> buf = new ArrayList<>(CHUNK);

    for (String id : cameraIds) {
      buf.add(id);
      if (buf.size() == CHUNK) {
        queryChunk(buf, out);
        buf.clear();
      }
    }
    if (!buf.isEmpty()) queryChunk(buf, out);
    return out;
  }

  private void queryChunk(List<String> ids, Map<String, Integer> out) {
    String placeholders = ids.stream().map(s -> "?").collect(Collectors.joining(","));

    String sql =
        "SELECT SPOT_CAMR_ID, MIN(LANE_NO) AS MIN_LANE_NO " +
            "FROM SOITGLANEINFO " +
            "WHERE VHNO_4K_DTTN_YN = 'Y' " +
            "  AND SPOT_CAMR_ID IN (" + placeholders + ") " +
            "GROUP BY SPOT_CAMR_ID";

    jdbcTemplate.query(sql, ids.toArray(), rs -> {
      String camId = rs.getString(1);
      int minLane = rs.getInt(2);
      if (!rs.wasNull()) {
        out.put(camId, minLane);
      }
    });
  }
}
