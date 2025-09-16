package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgrtmdtinfoRequest;

public record Detected2k4kMerged(
    String spotCamrId,
    String kncrCd,
    String laneNo,
    String turnTypeCd,
    Long turnDttnUnixTm,
    Float turnDttnSped,
    Long stlnPasgUnixTm,
    Float stlnDttnSped,
    Float vhclSectSped,
    Long frstObsrvnUnixTm,
    Long vhclObsrvnHr,
    String vhnoNm,
    String vhnoDttnYn,
    String imgPathNm,
    String vhclImgFileNm,
    String noplImgFileNm,
    String vhclDttnId
) {
    public static Detected2k4kMerged from(SoitgrtmdtinfoRequest request) {
        return new Detected2k4kMerged(
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
            request.getVhnoNm(),
            request.getVhnoDttnYn(),
            request.getImgPathNm(),
            request.getVhclImgFileNm(),
            request.getNoplImgFileNm(),
            request.getVhclDttnId()
        );
    }
}
