package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.Soitgrtmdtinfo_2K_Request;

public record DetectedVehicle2k(
    String spotCamrId,
    String kncrCd,
    Long laneNo,
    String turnTypeCd,
    Long turnDttnUnixTm,
    Float turnDttnSped,
    Long stlnPasgUnixTm,
    Float stlnDttnSped,
    Float vhclSectSped,
    Long frstObsrvnUnixTm,
    Long vhclObsrvnHr,
    String imgPathNm,
    String vhclImgFileNm,
    String vhclDttn2kId
) {
    public static DetectedVehicle2k from(Soitgrtmdtinfo_2K_Request request) {
        return new DetectedVehicle2k(
            request.getSpotCamrId(),
            request.getKncrCd(),
            request.getLaneNo(),
            request.getTurnTypeCd(),
            request.getTurnDttnUnixTm(),
            request.getTurnDttnSped(),
            request.getStlnPasgUnixTm(),
            request.getStlnDttnSped(),
            request.getVhclSectSped(),
            request.getFrstObsrvnUnixTm(),
            request.getVhclObsrvnHr(),
            request.getImgPathNm(),
            request.getVhclImgFileNm(),
            request.getVhclDttn2KId()
        );
    }
}
