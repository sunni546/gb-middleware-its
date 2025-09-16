package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgcamrsttsinfoRequest;

public record StatusEdgeAndCam(
    String spotCamrId,
    String instlLocnNo,
    Long prcsUnixTm,
    String camr2kSttsCd,
    String ecamr2kSttsCd,
    String camr4kSttsCd,
    String ecamr4kSttsCd
) {
    public static StatusEdgeAndCam from(SoitgcamrsttsinfoRequest request) {
        return new StatusEdgeAndCam(
            request.getSpotCamrId(),
            request.getInstlLocnNo(),
            request.getPrcsUnixTm(),
            request.getCamr2KSttsCd(),
            request.getEcamr2KSttsCd(),
            request.getCamr4KSttsCd(),
            request.getEcamr4KSttsCd()
        );
    }
}
