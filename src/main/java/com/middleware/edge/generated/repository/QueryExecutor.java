package com.middleware.edge.generated.repository;

import com.middleware.edge.generated.config.DB;
import java.sql.Statement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@Slf4j
public abstract class QueryExecutor {
  private final NamedParameterJdbcTemplate namedJdbcTemplate;


  public QueryExecutor(NamedParameterJdbcTemplate namedJdbcTemplate) {
    this.namedJdbcTemplate = namedJdbcTemplate;
  }

  public <T> void executeBatch(String sql, List<T> items) {
    int[] batched = namedJdbcTemplate.batchUpdate(sql, toSqlParam(items));
    Logger dbLogger = getDbLogger();
    int successCount = 0;
    String tableName = extractTableName(sql);
    
    for ( int i = 0; i < batched.length; i++ ) {
      if (batched[i] == Statement.SUCCESS_NO_INFO) {
        log.warn("{} query executed. But no affected rows info. \nRecord: {} \nsql: {}", getDb().getValue(), items.get(i), sql);
      } else if (batched[i] == Statement.EXECUTE_FAILED) {
        log.warn("{} query failed. \nRecord: {} \nsql: {}", getDb().getValue(), items.get(i), sql);
      } else if (batched[i] == 0) { // 0이면 쿼리 무시됨(키중복, 조건 불만족 등)
        log.debug("{} query executed but no rows affected (ignored). \nRecord: {} \nsql: {}", getDb().getValue(), items.get(i), sql);
      } else if (batched[i] == 1) { // 1이면 INSERT 정상 처리
        successCount++;
        dbLogger.info("DB: {} | Table: {} | Success: {}/{} records | Data: {}",
            getDb().getValue(), tableName, successCount, items.size(), items.get(i).toString());
      } else if (batched[i] == 2) { // 2이면 UPDATE 정상 처리 (ON DUPLICATE KEY UPDATE)
        successCount++;
        dbLogger.info("DB: {} | Table: {} | Success: {}/{} records | Data: {}",
            getDb().getValue(), tableName, successCount, items.size(), items.get(i).toString());
        log.debug("{} query executed with UPDATE operation (2 affected rows). \nRecord: {}", 
            getDb().getValue(), items.get(i));
      } else {
        // 그 외 (3 이상이거나 음수)는 비정상
        log.warn("{} query executed. But affected {} rows. \nRecord: {} \nsql: {}", getDb().getValue(), batched[i], items.get(i), sql);
      }
    }
  }

  public <T> void executeSingle(String sql, T item) {
    int affectedRows = namedJdbcTemplate.update(sql, toSqlParam(item));
    Logger dbLogger = getDbLogger();
    String tableName = extractTableName(sql);
    
    if (affectedRows == 0) {
      log.debug("{} single query executed but no rows affected (ignored). Table: {} | Record: {}",
          getDb().getValue(), tableName, item);
    } else if (affectedRows == 1) {
      // INSERT 정상 처리
      dbLogger.info("DB: {} | Table: {} | Success: 1 record | Data: {}",
          getDb().getValue(), tableName, item.toString());
    } else if (affectedRows == 2) {
      // UPDATE 정상 처리 (ON DUPLICATE KEY UPDATE)
      dbLogger.info("DB: {} | Table: {} | Success: 1 record | Data: {}",
          getDb().getValue(), tableName, item.toString());
      log.debug("{} single query executed with UPDATE operation (2 affected rows). Table: {} | Record: {}",
          getDb().getValue(), tableName, item);
    } else {
      // 그 외 (3 이상이거나 음수)는 비정상
      log.warn("{} single query executed but affected {} rows. Table: {} | Record: {}",
          getDb().getValue(), affectedRows, tableName, item);
    }
  }

  public abstract DB getDb();

  protected <T> SqlParameterSource[] toSqlParam(List<T> items) {
    return items.stream()
        .map(this::toSqlParam)
        .toArray(SqlParameterSource[]::new);
  }

  protected <T> SqlParameterSource toSqlParam(T item) {
    return new BeanPropertySqlParameterSource(item);
  }

  /**
   * 데이터베이스별 전용 로거를 반환합니다.
   */
  protected Logger getDbLogger() {
    return switch (getDb()) {
      case TIBERO -> LoggerFactory.getLogger("TIBERO_QUERY_LOGGER");
      case MARIADB -> LoggerFactory.getLogger("MARIA_QUERY_LOGGER");
      case VOLTDB -> LoggerFactory.getLogger("VOLT_QUERY_LOGGER");
    };
  }

  /**
   * SQL문에서 테이블 이름을 추출합니다.
   */
  private String extractTableName(String sql) {
    if (sql == null || sql.trim().isEmpty()) {
      return "UNKNOWN";
    }
    
    String upperSql = sql.toUpperCase().trim();
    if (upperSql.startsWith("INSERT")) {
      // INSERT INTO 테이블명 패턴 찾기
      String[] tokens = upperSql.split("\\s+");
      for (int i = 0; i < tokens.length - 1; i++) {
        if ("INTO".equals(tokens[i]) && i + 1 < tokens.length) {
          String tableName = tokens[i + 1];
          // 괄호나 특수문자 제거
          return tableName.replaceAll("[(),]", "").trim();
        }
      }
    } else if (upperSql.startsWith("UPSERT")) {
      // UPSERT INTO 테이블명 패턴 찾기 (VoltDB용)
      String[] tokens = upperSql.split("\\s+");
      for (int i = 0; i < tokens.length - 1; i++) {
        if ("INTO".equals(tokens[i]) && i + 1 < tokens.length) {
          String tableName = tokens[i + 1];
          // 괄호나 특수문자 제거
          return tableName.replaceAll("[(),]", "").trim();
        }
      }
    } else if (upperSql.startsWith("MERGE")) {
      // MERGE INTO 테이블명 패턴 찾기 (Tibero용)
      String[] tokens = upperSql.split("\\s+");
      for (int i = 0; i < tokens.length - 1; i++) {
        if ("INTO".equals(tokens[i]) && i + 1 < tokens.length) {
          String tableName = tokens[i + 1];
          // 괄호나 특수문자 제거
          return tableName.replaceAll("[(),]", "").trim();
        }
      }
    }
    
    return "UNKNOWN";
  }

}
