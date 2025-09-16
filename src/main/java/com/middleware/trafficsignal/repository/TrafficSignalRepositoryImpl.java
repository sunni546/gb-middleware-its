package com.middleware.trafficsignal.repository;

import com.middleware.common.aop.SqlRetryOnFailure;
import com.middleware.common.config.JdbcTemplateConfig;
import com.middleware.trafficsignal.model.TrafficSignalDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TrafficSignalRepositoryImpl implements TrafficSignalRepository {

  private final JdbcTemplate jdbcTemplate;

  public TrafficSignalRepositoryImpl(@Qualifier(JdbcTemplateConfig.VOLT_DB_TRAFFIC) JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @SqlRetryOnFailure
  public List<TrafficSignalDto> findAllTrafficSignals() {
    String sql = "SELECT SPOT_INTS_ID, A_RING_MVMT_NO, B_RING_MVMT_NO, CLCT_UNIX_TM FROM SOITDSPOTINTSSTTS";
    return jdbcTemplate.query(sql,
        (rs, rowNum) -> TrafficSignalDto.builder()
            .spotIntersectionId(rs.getString("SPOT_INTS_ID"))
            .collectedUnixTime(rs.getLong("CLCT_UNIX_TM"))
            .aRingMovementNumber(rs.getInt("A_RING_MVMT_NO"))
            .bRingMovementNumber(rs.getInt("B_RING_MVMT_NO"))
            .build()
    );
  }

//  @Scheduled(cron = "0 * * * * *")
//  // 신호 업데이트 될때마다 잘 전송되는지 확인하기 위해 주기적으로 이동류 코드 변경하는 코드
//  protected void updateTrafficSignal() {
//    long currentTimeMinutes = System.currentTimeMillis() / 60_000;
//    String modula = Long.toString(currentTimeMinutes % 2);
//    String sql = "UPDATE SOITDSPOTINTSSTTS SET A_RING_MVMT_NO = " + modula + " WHERE SPOT_INTS_ID = 9999";
//
//    jdbcTemplate.update(sql);
//    log.info("Traffic signal updated {}", modula);
//  }
}
