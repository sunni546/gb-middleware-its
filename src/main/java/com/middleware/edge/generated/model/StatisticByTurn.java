package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgturntypestatsRequest;

public record StatisticByTurn(
    String spotCamrId,
    Long hrTypeCd,
    Long turnTypeCd,
    Long statsBgngUnixTm,
    Long statsEndUnixTm,
    Long kncr1Trvl,
    Long kncr2Trvl,
    Long kncr3Trvl,
    Long kncr4Trvl,
    Long kncr5Trvl,
    Long kncr6Trvl,
    Float avgStlnDttnSped,
    Float avgSectSped
) {
    public static StatisticByTurn from(SoitgturntypestatsRequest request) {
        return new StatisticByTurn(
            request.getSpotCamrId(),
            request.getHrTypeCd(),
            request.getTurnTypeCd(),
            request.getStatsBgngUnixTm(),
            request.getStatsEndUnixTm(),
            request.getKncr1Trvl(),
            request.getKncr2Trvl(),
            request.getKncr3Trvl(),
            request.getKncr4Trvl(),
            request.getKncr5Trvl(),
            request.getKncr6Trvl(),
            request.getAvgStlnDttnSped(),
            request.getAvgSectSped()
        );
    }
}
