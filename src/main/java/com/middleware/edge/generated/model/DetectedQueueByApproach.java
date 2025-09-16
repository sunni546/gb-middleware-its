package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgaprdqueuRequest;

public record DetectedQueueByApproach(
    String spotCamrId,
    Long statsBgngUnixTm,
    Long statsEndUnixTm,
    Float rmnnQueuLngt,
    Float maxQueuLngt,
    String imgPathNm,
    String imgFileNm
) {
    public static DetectedQueueByApproach from(SoitgaprdqueuRequest request) {
        return new DetectedQueueByApproach(
            request.getSpotCamrId(),
            request.getStatsBgngUnixTm(),
            request.getStatsEndUnixTm(),
            request.getRmnnQueuLngt(),
            request.getMaxQueuLngt(),
            request.getImgPathNm(),
            request.getImgFileNm()
        );
    }
}
