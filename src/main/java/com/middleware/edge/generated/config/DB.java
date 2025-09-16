package com.middleware.edge.generated.config;

import lombok.Getter;

@Getter
public enum DB {
  TIBERO("Tibero"), MARIADB("MariaDb"), VOLTDB("Voltdb");
  private final String value;

  DB(String value) {
    this.value = value;
  }

  public static DB from(String db) {
    for (DB dbEnum : DB.values()) {
      if (dbEnum.value.equalsIgnoreCase(db)) {
        return dbEnum;
      }
    }
    throw new IllegalArgumentException("Unknown database name: " + db);
  }
}
