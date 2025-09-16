package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgaprdstatsRequest;

public record StatisticByApproach(
    String spotCamrId,
    Long hrTypeCd,
    Long statsBgngUnixTm,
    Long statsEndUnixTm,
    Long totlTrvl,
    Float avgStlnDttnSped,
    Float avgSectSped,
    Long avgTrfcDnst,
    Long minTrfcDnst,
    Long maxTrfcDnst,
    Float avgLaneOcpnRt
) {
    public static StatisticByApproach from(SoitgaprdstatsRequest request) {
        return new StatisticByApproach(
            request.getSpotCamrId(),
            request.getHrTypeCd(),
            request.getStatsBgngUnixTm(),
            request.getStatsEndUnixTm(),
            request.getTotlTrvl(),
            request.getAvgStlnDttnSped(),
            request.getAvgSectSped(),
            request.getAvgTrfcDnst(),
            request.getMinTrfcDnst(),
            request.getMaxTrfcDnst(),
            request.getAvgLaneOcpnRt()
        );
    }
}
