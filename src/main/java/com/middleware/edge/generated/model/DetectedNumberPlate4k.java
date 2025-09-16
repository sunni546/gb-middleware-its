package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.Soitgrtmdtinfo_4K_Request;

public record DetectedNumberPlate4k(
    String spotCamrId,
    String kncrCd,
    Long laneNo,
    Long stlnPasgUnixTm,
    String vhnoNm,
    String vhnoDttnYn,
    String imgPathNm,
    String vhclImgFileNm,
    String noplImgFileNm,
    String vhclDttn4kId
) {
    public static DetectedNumberPlate4k from(Soitgrtmdtinfo_4K_Request request) {
        return new DetectedNumberPlate4k(
            request.getSpotCamrId(),
            request.getKncrCd(),
            request.getLaneNo(),
            request.getStlnPasgUnixTm(),
            request.getVhnoNm(),
            request.getVhnoDttnYn(),
            request.getImgPathNm(),
            request.getVhclImgFileNm(),
            request.getNoplImgFileNm(),
            request.getVhclDttn4KId()
        );
    }
}
