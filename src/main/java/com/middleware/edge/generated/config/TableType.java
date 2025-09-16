package com.middleware.edge.generated.config;

import com.middleware.edge.generated.model.Detected2k4kMerged;
import com.middleware.edge.generated.model.DetectedEvent;
import com.middleware.edge.generated.model.DetectedNumberPlate4k;
import com.middleware.edge.generated.model.DetectedPedestrian;
import com.middleware.edge.generated.model.DetectedQueueByApproach;
import com.middleware.edge.generated.model.DetectedQueueByLane;
import com.middleware.edge.generated.model.DetectedVehicle2k;
import com.middleware.edge.generated.model.StatisticByApproach;
import com.middleware.edge.generated.model.StatisticByLane;
import com.middleware.edge.generated.model.StatisticByTurn;
import com.middleware.edge.generated.model.StatisticByVehicleType;
import com.middleware.edge.generated.model.StatusEdgeAndCam;
import com.middleware.edge.generated.model.StatusEnclosure;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum TableType {
  STATISTIC_BY_APPROACH(StatisticByApproach.class, "soitgaprdstats"),
  STATISTIC_BY_TURN(StatisticByTurn.class, "soitgturntypestats"),
  STATISTIC_BY_VEHICLE_TYPE(StatisticByVehicleType.class, "soitgkncrstats"),
  STATISTIC_BY_LANE(StatisticByLane.class, "soitglanestats"),
  DETECTED_PEDESTRIAN(DetectedPedestrian.class, "soitgcwdtinfo"),
  DETECTED_QUEUE_BY_APPROACH(DetectedQueueByApproach.class, "soitgaprdqueu"),
  DETECTED_QUEUE_BY_LANE(DetectedQueueByLane.class, "soitglanequeu"),
  DETECTED_EVENT(DetectedEvent.class, "soitgunacevet"),
  DETECTED_VEHICLE_2K(DetectedVehicle2k.class, "soitgrtmdtinfo_2K"),
  STATUS_ENCLOSURE(StatusEnclosure.class, "soitgenclsttsinfo"),
  STATUS_EDGE_AND_CAM(StatusEdgeAndCam.class, "soitgcamrsttsinfo"),
  DETECTED_2K_4K_MERGED(Detected2k4kMerged.class, "soitgrtmdtinfo"),
  DETECTED_NUMBER_PLATE_4K(DetectedNumberPlate4k.class, "soitgrtmdtinfo_4K");

  private final Class<?> clazz;
  private final String tableName;

  TableType(Class<?> clazz, String tableName) {
    this.clazz = clazz;
    this.tableName = tableName;
  }
  public static TableType fromTableName(String tableName) {
    for (TableType mapping : TableType.values()) {
      if (mapping.tableName.equals(tableName)) {
        return mapping;
      }
    }
    throw new IllegalArgumentException("Unknown table name for " + tableName);
  }
  public static TableType fromClass(Class<?> clazz) {
    for (TableType mapping : TableType.values()) {
      if (mapping.clazz == clazz) {
        return mapping;
      }
    }
    throw new IllegalArgumentException("Unknown type for " + clazz);
  }
}