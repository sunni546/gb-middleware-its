package com.middleware.edge.selfinfo.model;

import lombok.Builder;

/**
 *
 */
@Builder
public record LaneDto(
    String spotCameraId,
    int laneNo,
    String vhno4kDttnYn
) {}
