package com.middleware.edge.generated.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EdgeDataSaveExecutorConfig {
  public final static String MARIADB = "mariadbExecutor";
  public final static String TIBERO = "tiberoExecutor";
  public final static String VOLTDB = "voltdbExecutor";


  @Value("${datasource.mariadb.hikari.maximum-pool-size}")
  private int mariadbPoolSize;

  @Value("${datasource.tibero.hikari.maximum-pool-size}")
  private int tiberoPoolSize;

  @Value("${datasource.voltdb.default.hikari.maximum-pool-size}")
  private int voltdbPoolSize;

  @Bean(name = MARIADB)
  public Executor mariadbExecutor() {
    return createExecutor("mariadb-", mariadbPoolSize);
  }

  @Bean(name = TIBERO)
  public Executor tiberoExecutor() {
    return createExecutor("tibero-", tiberoPoolSize);
  }

  @Bean(name = VOLTDB)
  public Executor voltdbExecutor() {
    return createExecutor("voltdb-", voltdbPoolSize);
  }

  private Executor createExecutor(String prefix, int poolSize) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setQueueCapacity(1000); // 기본값, 필요 시 설정 가능
    executor.setThreadNamePrefix(prefix);
    executor.initialize();
    return executor;
  }
}
