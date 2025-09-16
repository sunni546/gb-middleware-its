package com.middleware.edge.selfinfo.model;

import lombok.Builder;

/**
 * Edge의 Python 코드상에서 VoltDb 149의 SOITGCAMRINFO 테이블을 조회하는 정보를 담은 객체
 */
@Builder
public record EdgeInfo(
    String spotIntersectionId,
    String spotCameraId,
    String ecuInfoTransmissionYesOrNo,
    String installedLocationNumber
) {
  public static EdgeInfo from(EdgeCache edgeInfo) {
    return EdgeInfo.builder()
        .spotIntersectionId(edgeInfo.spotIntersectionId())
        .spotCameraId(edgeInfo.spotCameraId())
        .installedLocationNumber(edgeInfo.installedLocationNumber())
        .ecuInfoTransmissionYesOrNo(edgeInfo.ecuInfoTransmissionYesOrNo())
        .build();
  }
}
