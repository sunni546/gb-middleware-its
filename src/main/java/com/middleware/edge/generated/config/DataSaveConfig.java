package com.middleware.edge.generated.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "table-config")
public record DataSaveConfig (
    Set<EdgeDataSaveStrategy> saveStrategies
) {
  @ConstructorBinding
  public DataSaveConfig(Set<EdgeDataSaveStrategy> saveStrategies) {
    this.saveStrategies = Collections.unmodifiableSet(saveStrategies);
  }

  public record EdgeDataSaveStrategy(
      long drainMaxMillis,
      int maxSize,
      List<SqlPerDb> queries,
      Class<?> clazz
  ) {
    @ConstructorBinding
    public EdgeDataSaveStrategy(String tableName, int maxSize, long drainMaxMillis, List<SqlPerDb> queries) {
      this(drainMaxMillis, maxSize,
          Collections.unmodifiableList(queries),
          TableType.fromTableName(tableName).getClazz());
    }

    public record SqlPerDb(DB db, String sql) {
      @ConstructorBinding
      public SqlPerDb(String db, String sql) {
        this(DB.from(db), sql);
      }
    }
  }

}
