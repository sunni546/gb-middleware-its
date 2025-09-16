package com.middleware.common.config;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

@Slf4j
@Configuration
public class JdbcTemplateConfig {

  public static final String TIBERO_NAMED = "tiberoNamedParameterJdbcTemplate";
  public static final String MARIA_DB_NAMED = "mariaDbNamedParameterJdbcTemplate";
  public static final String VOLT_DB_DEFAULT = "voltDbDefaultJdbcTemplate";
  public static final String VOLT_DB_DEFAULT_NAMED = "voltDbDefaultNamedParameterJdbcTemplate";
  public static final String VOLT_DB_TRAFFIC = "voltDbTrafficJdbcTemplate";

  @Primary
  @Bean
  @ConfigurationProperties(prefix = "datasource.tibero")
  public DataSource tiberoDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public JdbcTemplate tiberoJdbcTemplate() {
    return new JdbcTemplate(tiberoDataSource());
  }

  @Bean(JdbcTemplateConfig.TIBERO_NAMED)
  public NamedParameterJdbcTemplate tiberoNamedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(tiberoDataSource());
  }

  @Primary
  @Bean
  public TransactionManager tiberoTransactionManager() {
    return new DataSourceTransactionManager(tiberoDataSource());
  }

  @Bean
  @ConfigurationProperties(prefix = "datasource.mariadb")
  public DataSource mariaDbDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public JdbcTemplate mariaDbJdbcTemplate() {
    return new JdbcTemplate(mariaDbDataSource());
  }

  @Bean(JdbcTemplateConfig.MARIA_DB_NAMED)
  public NamedParameterJdbcTemplate mariaDbNamedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(mariaDbDataSource());
  }

  @Bean
  public TransactionManager mariaDbTransactionManager() {
    return new DataSourceTransactionManager(mariaDbDataSource());
  }

  @Bean
  @ConfigurationProperties(prefix = "datasource.voltdb.traffic")
  public DataSource voltDbTrafficDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public JdbcTemplate voltDbTrafficJdbcTemplate() {
    return new JdbcTemplate(voltDbTrafficDataSource());
  }

  @Bean
  public NamedParameterJdbcTemplate voltDbTrafficNamedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(voltDbTrafficDataSource());
  }

  @Bean
  public TransactionManager voltDbTrafficTransactionManager() {
    return new DataSourceTransactionManager(voltDbTrafficDataSource());
  }

  @Bean
  @ConfigurationProperties(prefix = "datasource.voltdb.default")
  public DataSource voltDbDefaultDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(JdbcTemplateConfig.VOLT_DB_DEFAULT_NAMED)
  public NamedParameterJdbcTemplate voltDbDefaultNamedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(voltDbDefaultDataSource());
  }

  @Bean
  public JdbcTemplate voltDbDefaultJdbcTemplate() {
    return new JdbcTemplate(voltDbDefaultDataSource());
  }

  @Bean
  public TransactionManager voltDbDefaultTransactionManager() {
    return new DataSourceTransactionManager(voltDbDefaultDataSource());
  }

}
