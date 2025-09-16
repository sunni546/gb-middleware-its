package com.middleware.grpc;

import static com.middleware.edge.selfinfo.model.EdgeCache.EdgeType.EDGE_2K;
import static com.middleware.edge.selfinfo.model.EdgeCache.EdgeType.EDGE_4K;

import com.google.protobuf.Empty;
import com.middleware.common.interceptor.ClientIpProvidingGrpcServerInterceptor;
import com.middleware.edge.selfinfo.EdgeService;
import com.middleware.edge.selfinfo.model.EdgeCache;
import com.middleware.edge.selfinfo.model.EdgeCache.EdgeType;
import com.middleware.edge.selfinfo.model.EdgeInfo;
import com.middleware.grpc.EdgeInfoResOuterClass.EdgeInfoRes;
import com.middleware.grpc.IpsInSpotResOuterClass.IpsInSpotRes;
import com.middleware.grpc.SpotCameraIdResOuterClass.SpotCameraIdRes;
import io.grpc.stub.StreamObserver;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Edge가 스스로의 정보를 VoltDb 149의 SOITGCAMRINFO 테이블을 통해 조회하는 기능의 grpc 인터페이스
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "edge.self-info.enabled", havingValue = "true", matchIfMissing = false)
public class EdgeInfoGrpcService extends EdgeInfoServiceGrpc.EdgeInfoServiceImplBase {
  private final ClientIpProvidingGrpcServerInterceptor clientIpProvider;

  private final EdgeService edgeService;

  @Override
  public void getSpotCameraId(
      com.google.protobuf.Empty request, StreamObserver<SpotCameraIdRes> responseObserver) {
    String clientIp = clientIpProvider.getClientIp();
    log.error("client ip : {}", clientIp);

    String spotCameraId = edgeService.getCameraIdByIp(clientIp);

    responseObserver.onNext(
        SpotCameraIdRes.newBuilder()
            .setSpotCameraId(spotCameraId)
            .build());
    responseObserver.onCompleted();
  }

  @Override
  public void getEdgeInfo(
      com.google.protobuf.Empty request, StreamObserver<EdgeInfoRes> responseObserver) {
    String clientIp = clientIpProvider.getClientIp();
    log.error("client ip : {}", clientIp);

    EdgeInfo edgeInfo = edgeService.getEdgeInfoByIp(clientIp);
    int laneOffset = edgeService.getLaneOffsetByIp(clientIp);

    responseObserver.onNext(
        EdgeInfoRes.newBuilder()
            .setSpotCameraId(edgeInfo.spotCameraId())
            .setInstalledLocationNumber(edgeInfo.installedLocationNumber())
            .setEcuInfoTransmissionYesOrNo(edgeInfo.ecuInfoTransmissionYesOrNo())
            .setSpotIntersectionId(edgeInfo.spotIntersectionId())
            .setLaneOffset(laneOffset)
            .build()
    );
    responseObserver.onCompleted();
  }

  @Override
  public void getAllIpsInSpot(Empty request, StreamObserver<IpsInSpotRes> responseObserver) {
    String clientIp = clientIpProvider.getClientIp();
    Set<EdgeCache> edges = edgeService.getEdgesInSpot(clientIp);
    Function<EdgeType, String> getIp = (type) ->  {
      for (EdgeCache edge : edges) {
        if (edge.type() == type) {
          return edge.ip();
        }
      }
      return null;
    };

    responseObserver.onNext(
        IpsInSpotRes.newBuilder()
            .setEdge2KIp(getIp.apply(EDGE_2K))
            .setEdge4KIp(getIp.apply(EDGE_4K))
            .setCamera2KIp(getIp.apply(EdgeType.CAMERA_2K))
            .setCamera4KIp(getIp.apply(EdgeType.CAMERA_4K))
            .build()
    );
    responseObserver.onCompleted();
  }
}
