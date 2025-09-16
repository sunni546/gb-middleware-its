package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgenclsttsinfoRequest;

public record StatusEnclosure(
    String spotCamrId,
    Long prcsUnixTm,
    String cnmcSttsCd,
    String eqpmSttsCd,
    String eqpmTmpt,
    String pannSttsCd,
    String hetrSttsCd,
    String enclOpenYn
) {
    public static StatusEnclosure from(SoitgenclsttsinfoRequest request) {
        return new StatusEnclosure(
            request.getSpotCamrId(),
            request.getPrcsUnixTm(),
            request.getCnmcSttsCd(),
            request.getEqpmSttsCd(),
            request.getEqpmTmpt(),
            request.getPannSttsCd(),
            request.getHetrSttsCd(),
            request.getEnclOpenYn()
        );
    }
}
