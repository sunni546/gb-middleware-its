package com.middleware.edge.generated.repository;

import com.middleware.common.aop.SqlRetryOnFailure;
import com.middleware.common.config.JdbcTemplateConfig;
import com.middleware.edge.generated.config.DB;
import com.middleware.edge.generated.config.EdgeDataSaveExecutorConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@Slf4j
public class TiberoQueryExecutor extends QueryExecutor {

  public TiberoQueryExecutor(@Qualifier(JdbcTemplateConfig.TIBERO_NAMED) NamedParameterJdbcTemplate namedJdbcTemplate) {
    super(namedJdbcTemplate);
  }

  @Override
  @SqlRetryOnFailure(retryAttempts = 1)
  @Async(EdgeDataSaveExecutorConfig.TIBERO)
  public <T> void executeBatch(String sql, List<T> items) {
    super.executeBatch(sql, items);
  }

  @Override
  @SqlRetryOnFailure(retryAttempts = 1)
  @Async(EdgeDataSaveExecutorConfig.TIBERO)
  public <T> void executeSingle(String sql, T item) {
    super.executeSingle(sql, item);
  }

  @Override
  public DB getDb() {
    return DB.TIBERO;
  }
}
