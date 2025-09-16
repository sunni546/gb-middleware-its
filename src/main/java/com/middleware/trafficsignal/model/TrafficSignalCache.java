package com.middleware.trafficsignal.model;

import java.util.Objects;
import lombok.Builder;

@Builder
public record TrafficSignalCache(
    String spotIntersectionId,
    Long collectedUnixTime,
    Integer aRingMovementNumber,
    Integer bRingMovementNumber
) {
  public static TrafficSignalCache from(TrafficSignalDto dto) {
    return TrafficSignalCache.builder()
        .collectedUnixTime(dto.collectedUnixTime())
        .spotIntersectionId(dto.spotIntersectionId())
        .aRingMovementNumber(dto.aRingMovementNumber())
        .bRingMovementNumber(dto.bRingMovementNumber())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TrafficSignalCache that)) {
      return false;
    }
    return Objects.equals(spotIntersectionId, that.spotIntersectionId)
        && Objects.equals(aRingMovementNumber, that.aRingMovementNumber)
        && Objects.equals(bRingMovementNumber, that.bRingMovementNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spotIntersectionId, aRingMovementNumber, bRingMovementNumber);
  }
}

