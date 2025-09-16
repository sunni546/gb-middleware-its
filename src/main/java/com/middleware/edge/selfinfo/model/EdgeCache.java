package com.middleware.edge.selfinfo.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;

/**
 * VoltDb 149의 SOITGCAMRINFO 테이블에서 Redis에 캐싱할 정보를 담은 객체
 */
@Builder
public record EdgeCache (
    String ip,
    EdgeType type,
    String spotCameraId,
    String spotIntersectionId,
    String ecuInfoTransmissionYesOrNo,
    String installedLocationNumber,
    int laneOffset
) {
  public enum EdgeType {
    EDGE_2K, EDGE_4K, CAMERA_2K, CAMERA_4K;
  }

  /**
   * CameraDto → EdgeCache 변환 (laneOffset까지 반영).
   *
   * @param cameraDto 카메라 정보
   * @param minLaneMap 카메라ID별 최소 차로번호 맵 (4K만 대상)
   * @param overrideByIp IP별 강제 laneOffset 맵 (설정 기반)
   */
  public static List<EdgeCache> from(
      CameraDto cameraDto,
      Map<String, Integer> minLaneMap,
      Map<String, Integer> overrideByIp
  ) {
    return Arrays.stream(EdgeType.values())
        .map(type -> {
          String ip = switch (type) {
            case EDGE_2K   -> cameraDto.edge2kIp();
            case EDGE_4K   -> cameraDto.edge4kIp();
            case CAMERA_2K -> cameraDto.camera2KIp();
            case CAMERA_4K -> cameraDto.camera4KIp();
          };

          if (ip == null) return null;

          int laneOffset = -1;
          if (overrideByIp.containsKey(ip)) {
            laneOffset = overrideByIp.get(ip);
          } else if (type == EdgeType.EDGE_4K && cameraDto.spotCameraId() != null) {
            int minLane = minLaneMap.getOrDefault(cameraDto.spotCameraId(), 1);
            laneOffset = Math.max(0, minLane - 1);
          }

          return EdgeCache.builder()
              .ip(ip)
              .type(type)
              .spotCameraId(cameraDto.spotCameraId())
              .spotIntersectionId(cameraDto.spotIntersectionId())
              .ecuInfoTransmissionYesOrNo(cameraDto.ecuInfoTransmissionYesOrNo())
              .installedLocationNumber(cameraDto.installedLocationNumber())
              .laneOffset(laneOffset)
              .build();
        })
        .filter(Objects::nonNull)
        .toList();
  }
}
