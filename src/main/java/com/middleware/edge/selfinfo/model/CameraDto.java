package com.middleware.edge.selfinfo.model;

import lombok.Builder;

/**
 *
 */
@Builder
public record CameraDto(
    String edge2kIp,
    String edge4kIp,
    String camera2KIp,
    String camera4KIp,
    String spotIntersectionId,
    String spotCameraId,
    String ecuInfoTransmissionYesOrNo,
    String installedLocationNumber
) {}
