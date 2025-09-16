package com.middleware.trafficsignal.model;

import java.util.Objects;
import lombok.Builder;

@Builder
public record TrafficSignalDto(
    String spotIntersectionId,
    Long collectedUnixTime,
    Integer aRingMovementNumber,
    Integer bRingMovementNumber
) {
  public static TrafficSignalDto from(TrafficSignalCache cache) {
    return TrafficSignalDto.builder()
        .spotIntersectionId(cache.spotIntersectionId())
        .collectedUnixTime(cache.collectedUnixTime())
        .aRingMovementNumber(cache.aRingMovementNumber())
        .bRingMovementNumber(cache.bRingMovementNumber())
        .build();
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TrafficSignalDto that)) {
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
