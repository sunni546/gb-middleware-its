package com.middleware.edge.generated.model;

import com.middleware.grpc.EdgeSaveProto.SoitgkncrstatsRequest;

public record StatisticByVehicleType(
    String spotCamrId,
    Long hrTypeCd,
    String kncrCd,
    Long statsBgngUnixTm,
    Long statsEndUnixTm,
    Long totlTrvl,
    Float avgStlnDttnSped,
    Float avgSectSped
) {
    public static StatisticByVehicleType from(SoitgkncrstatsRequest request) {
        return new StatisticByVehicleType(
            request.getSpotCamrId(),
            request.getHrTypeCd(),
            request.getKncrCd(),
            request.getStatsBgngUnixTm(),
            request.getStatsEndUnixTm(),
            request.getTotlTrvl(),
            request.getAvgStlnDttnSped(),
            request.getAvgSectSped()
        );
    }
}
