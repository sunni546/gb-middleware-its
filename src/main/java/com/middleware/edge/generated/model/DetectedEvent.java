package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.Soitgunacevet_S_Request;
import com.middleware.grpc.EdgeSaveProto.Soitgunacevet_C_Request;
import com.middleware.grpc.EdgeSaveProto.Soitgunacevet_E_Request;

public record DetectedEvent(
    String spotCamrId,
    Long trceId,
    Long ocrnUnixTm,
    String evetTypeCd,
    String imgPathNm,
    String imgFileNm,
    Long prcsUnixTm,
    Long endUnixTm,
    Long crtUnixTm
) {
    public static DetectedEvent fromStart(Soitgunacevet_S_Request request) {
        return new DetectedEvent(
            request.getSpotCamrId(),
            request.getTrceId(),
            request.getOcrnUnixTm(),
            request.getEvetTypeCd(),
            request.getImgPathNm(),
            request.getImgFileNm(),
            null,
            null,
            null
        );
    }
    
    public static DetectedEvent fromContinue(Soitgunacevet_C_Request request) {
        return new DetectedEvent(
            request.getSpotCamrId(),
            request.getTrceId(),
            request.getOcrnUnixTm(),
            null,
            null,
            null,
            request.getPrcsUnixTm(),
            null,
            null
        );
    }
    
    public static DetectedEvent fromEnd(Soitgunacevet_E_Request request) {
        return new DetectedEvent(
            request.getSpotCamrId(),
            request.getTrceId(),
            request.getOcrnUnixTm(),
            null,
            null,
            null,
            null,
            request.getEndUnixTm(),
            null
        );
    }
}
