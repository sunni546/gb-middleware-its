package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitglanestatsRequest;

public record StatisticByLane(
    String spotCamrId,
    Long hrTypeCd,
    Long laneNo,
    Long statsBgngUnixTm,
    Long statsEndUnixTm,
    Long totlTrvl,
    Float avgStlnDttnSped,
    Float avgSectSped,
    Long avgTrfcDnst,
    Long minTrfcDnst,
    Long maxTrfcDnst,
    Float ocpnRt
) {
    public static StatisticByLane from(SoitglanestatsRequest request) {
        return new StatisticByLane(
            request.getSpotCamrId(),
            request.getHrTypeCd(),
            request.getLaneNo(),
            request.getStatsBgngUnixTm(),
            request.getStatsEndUnixTm(),
            request.getTotlTrvl(),
            request.getAvgStlnDttnSped(),
            request.getAvgSectSped(),
            request.getAvgTrfcDnst(),
            request.getMinTrfcDnst(),
            request.getMaxTrfcDnst(),
            request.getOcpnRt()
        );
    }
}
