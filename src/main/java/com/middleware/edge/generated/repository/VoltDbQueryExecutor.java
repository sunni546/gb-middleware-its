package com.middleware.edge.generated.repository;

import com.middleware.common.aop.SqlRetryOnFailure;
import com.middleware.common.config.JdbcTemplateConfig;
import com.middleware.edge.generated.config.DB;
import com.middleware.edge.generated.config.EdgeDataSaveExecutorConfig;
import java.sql.Types;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@Slf4j
public class VoltDbQueryExecutor extends QueryExecutor {

  public VoltDbQueryExecutor(@Qualifier(JdbcTemplateConfig.VOLT_DB_DEFAULT_NAMED) NamedParameterJdbcTemplate namedJdbcTemplate) {
    super(namedJdbcTemplate);
  }

  @Override
  @SqlRetryOnFailure(retryAttempts = 1)
  @Async(EdgeDataSaveExecutorConfig.VOLTDB)
  public <T> void executeBatch(String sql, List<T> items) {
    super.executeBatch(sql, items);
  }

  @Override
  @SqlRetryOnFailure(retryAttempts = 1)
  @Async(EdgeDataSaveExecutorConfig.VOLTDB)
  public <T> void executeSingle(String sql, T item) {
    super.executeSingle(sql, item);
  }

  @Override
  public DB getDb() {
    return DB.VOLTDB;
  }

  @Override
  protected <T> SqlParameterSource toSqlParam(T item) {
    return handleVoltDbRecord(item);
  }

  private SqlParameterSource handleVoltDbRecord(Object item) {
    var source = new MapSqlParameterSource();
    Class<?> clazz = item.getClass();

    // voltdb는 float 타입을 지원하지 않아 에러 발생, Double로 캐스팅
    for (var method : clazz.getDeclaredMethods()) {
      if (method.getParameterCount() == 0 &&
          !method.getName().equals("<init>") &&
          method.getReturnType() != void.class) {
        try {
          Object value = method.invoke(item);
          String paramName = method.getName();

          if (value == null) {
            source.addValue(paramName, null);
            continue;
          }
          
          switch (value) {
            case Float f -> source.addValue(paramName, (double) f, Types.DOUBLE);
            case Double d -> source.addValue(paramName, d, Types.DOUBLE);
            default -> source.addValue(paramName, value);
          }

        } catch (Exception e) {
          log.warn("Failed to invoke method {} on record {}", method.getName(), clazz.getName(), e);
        }
      }
    }

    return source;
  }
}
