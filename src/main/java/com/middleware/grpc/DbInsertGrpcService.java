package com.middleware.grpc;

import com.middleware.common.interceptor.ClientIpProvidingGrpcServerInterceptor;
import com.middleware.edge.generated.model.Detected2k4kMerged;
import com.middleware.edge.generated.model.DetectedEvent;
import com.middleware.edge.generated.model.DetectedNumberPlate4k;
import com.middleware.edge.generated.model.DetectedPedestrian;
import com.middleware.edge.generated.model.DetectedQueueByApproach;
import com.middleware.edge.generated.model.DetectedQueueByLane;
import com.middleware.edge.generated.model.DetectedVehicle2k;
import com.middleware.edge.generated.model.StatisticByApproach;
import com.middleware.edge.generated.model.StatisticByLane;
import com.middleware.edge.generated.model.StatisticByTurn;
import com.middleware.edge.generated.model.StatisticByVehicleType;
import com.middleware.edge.generated.model.StatusEdgeAndCam;
import com.middleware.edge.generated.model.StatusEnclosure;
import com.middleware.edge.generated.service.EdgeDataQueueManager;
import com.middleware.grpc.EdgeSaveProto.EdgeReply;
import com.middleware.grpc.EdgeSaveProto.SoitgaprdqueuRequest;
import com.middleware.grpc.EdgeSaveProto.SoitgaprdstatsRequest;
import com.middleware.grpc.EdgeSaveProto.SoitgcamrsttsinfoRequest;
import com.middleware.grpc.EdgeSaveProto.SoitgcwdtinfoRequest;
import com.middleware.grpc.EdgeSaveProto.SoitgenclsttsinfoRequest;
import com.middleware.grpc.EdgeSaveProto.SoitgkncrstatsRequest;
import com.middleware.grpc.EdgeSaveProto.SoitglanequeuRequest;
import com.middleware.grpc.EdgeSaveProto.SoitglanestatsRequest;
import com.middleware.grpc.EdgeSaveProto.SoitgrtmdtinfoRequest;
import com.middleware.grpc.EdgeSaveProto.Soitgrtmdtinfo_2K_Request;
import com.middleware.grpc.EdgeSaveProto.Soitgrtmdtinfo_4K_Request;
import com.middleware.grpc.EdgeSaveProto.SoitgturntypestatsRequest;
import com.middleware.grpc.EdgeSaveProto.Soitgunacevet_C_Request;
import com.middleware.grpc.EdgeSaveProto.Soitgunacevet_E_Request;
import com.middleware.grpc.EdgeSaveProto.Soitgunacevet_S_Request;
import io.grpc.stub.StreamObserver;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "edge.generated.enabled", havingValue = "true", matchIfMissing = false)
public class DbInsertGrpcService extends EdgeDataServiceGrpc.EdgeDataServiceImplBase {
  private final EdgeDataQueueManager queueManager;
  private final ClientIpProvidingGrpcServerInterceptor ipProvider;
  private static final Logger edgeRequestLogger = LoggerFactory.getLogger("EDGE_REQUEST_LOGGER");


  @Override
  public void saveSoitgaprdstats(SoitgaprdstatsRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, StatisticByApproach::from);
  }

  @Override
  public void saveSoitgturntypestats(SoitgturntypestatsRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, StatisticByTurn::from);
  }

  @Override
  public void saveSoitgkncrstats(SoitgkncrstatsRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, StatisticByVehicleType::from);
  }

  @Override
  public void saveSoitglanestats(SoitglanestatsRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, StatisticByLane::from);
  }

  @Override
  public void saveSoitgcwdtinfo(SoitgcwdtinfoRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedPedestrian::from);
  }

  @Override
  public void saveSoitgaprdqueu(SoitgaprdqueuRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedQueueByApproach::from);
  }

  @Override
  public void saveSoitglanequeu(SoitglanequeuRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedQueueByLane::from);
  }

  @Override
  public void saveSoitgunacevetS(Soitgunacevet_S_Request request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedEvent::fromStart);
  }

  @Override
  public void saveSoitgunacevetC(Soitgunacevet_C_Request request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedEvent::fromContinue);
  }

  @Override
  public void saveSoitgunacevetE(Soitgunacevet_E_Request request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedEvent::fromEnd);
  }

  @Override
  public void saveSoitgrtmdtinfo2K(Soitgrtmdtinfo_2K_Request request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedVehicle2k::from);
  }

  @Override
  public void saveSoitgenclsttsinfo(SoitgenclsttsinfoRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, StatusEnclosure::from);
  }

  @Override
  public void saveSoitgcamrsttsinfo(SoitgcamrsttsinfoRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, StatusEdgeAndCam::from);
  }

  @Override
  public void saveSoitgrtmdtinfo(SoitgrtmdtinfoRequest request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, Detected2k4kMerged::from);
  }

  @Override
  public void saveSoitgrtmdtinfo4K(Soitgrtmdtinfo_4K_Request request,
      StreamObserver<EdgeReply> responseObserver) {
    processUnaryRequestWithEdgeReply(request, responseObserver, DetectedNumberPlate4k::from);
  }

  private <T, R> void processUnaryRequestWithEdgeReply(T request, StreamObserver<EdgeReply> responseObserver, Function<T, R> mapper) {
    String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    log.info("Edge data request received from client ip : {} for method: {}", 
             ipProvider.getClientIp(), methodName);

    // 엣지 요청 정보를 별도 로그 파일에 기록
    logEdgeRequest(ipProvider.getClientIp(), methodName, request);

    try {
      queueManager.enqueueAndDrainIfNeeded(mapper.apply(request));

      EdgeReply reply = createSuccessReply();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();

      // log.info("Successfully processed {} and sent EdgeReply to client", methodName);
    } catch (Exception e) {
      log.error("Error processing {}: {}", methodName, e.getMessage(), e);

      EdgeReply errorReply = createErrorReply(500, "데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
      responseObserver.onNext(errorReply);
      responseObserver.onCompleted();
    }
  }

  private EdgeReply createSuccessReply() {
    return EdgeReply.newBuilder()
        .setStatusCode(200)
        .setMessage("데이터가 성공적으로 처리되었습니다.")
        .build();
  }

  private EdgeReply createErrorReply(int statusCode, String message) {
    return EdgeReply.newBuilder()
        .setStatusCode(statusCode)
        .setMessage(message)
        .build();
  }

  private void logEdgeRequest(String clientIp, String methodName, Object request) {
    String spotCamrId = extractFieldValue(request, "getSpotCamrId", "spot_camr_id");
    String uniqueKey = extractFieldValue(request, "getUniqueKey", "unique_key");

    edgeRequestLogger.info("CLIENT_IP: {} | METHOD: {} | SPOT_CAMR_ID: {} | UNIQUE_KEY: {}",
        clientIp, methodName, spotCamrId, uniqueKey);
  }

  protected String extractFieldValue(Object request, String methodName, String fieldName) {
    try {
      var method = request.getClass().getMethod(methodName);
      var result = method.invoke(request);
      return result != null ? result.toString() : "UNKNOWN";
    } catch (Exception e) {
      log.warn("Failed to extract {} from request: {}", fieldName, e.getMessage());
      return "UNKNOWN";
    }
  }
}
