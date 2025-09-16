package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgcwdtinfoRequest;

public record DetectedPedestrian(
    String spotCamrId,
    Long trceId,
    Long dttnUnixTm,
    String drctSeCd
) {
    public static DetectedPedestrian from(SoitgcwdtinfoRequest request) {
        return new DetectedPedestrian(
            request.getSpotCamrId(),
            request.getTrceId(),
            request.getDttnUnixTm(),
            request.getDrctSeCd()
        );
    }
}
