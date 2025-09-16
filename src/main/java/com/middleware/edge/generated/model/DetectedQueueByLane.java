package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitglanequeuRequest;

public record DetectedQueueByLane(
    String spotCamrId,
    Long laneNo,
    Long statsBgngUnixTm,
    Long statsEndUnixTm,
    Float rmnnQueuLngt,
    Float maxQueuLngt,
    String imgPathNm,
    String imgFileNm
) {
    public static DetectedQueueByLane from(SoitglanequeuRequest request) {
        return new DetectedQueueByLane(
            request.getSpotCamrId(),
            request.getLaneNo(),
            request.getStatsBgngUnixTm(),
            request.getStatsEndUnixTm(),
            request.getRmnnQueuLngt(),
            request.getMaxQueuLngt(),
            request.getImgPathNm(),
            request.getImgFileNm()
        );
    }
}
