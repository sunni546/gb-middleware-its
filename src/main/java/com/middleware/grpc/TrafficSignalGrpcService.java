package com.middleware.grpc;

import com.middleware.common.interceptor.ClientIpProvidingGrpcServerInterceptor;
import com.middleware.edge.selfinfo.EdgeService;
import com.middleware.grpc.TrafficSignalResOuterClass.TrafficSignalRes;
import com.middleware.trafficsignal.TrafficSignalService;
import com.middleware.trafficsignal.model.TrafficConnectionKey;
import com.middleware.trafficsignal.model.TrafficSignalDto;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@RequiredArgsConstructor
@GrpcService
@ConditionalOnProperty(name = {"traffic-signal.enabled", "edge.self-info.enabled"}, havingValue = "true", matchIfMissing = false)
public class TrafficSignalGrpcService extends TrafficSignalServiceGrpc.TrafficSignalServiceImplBase {

  private final TrafficSignalService trafficSignalService;
  private final EdgeService edgeService;
  private final ClientIpProvidingGrpcServerInterceptor clientIpProvider;

  private final Map<TrafficConnectionKey, StreamObserver<TrafficSignalRes>> streamsByConnection = new ConcurrentHashMap<>();
  private final Map<String, Set<TrafficConnectionKey>> connectionsByIntersectionId = new ConcurrentHashMap<>();

  private final Lock lock = new ReentrantLock();

  @Override
  public void subscribeTrafficSignal(com.google.protobuf.Empty request, StreamObserver<TrafficSignalRes> responseObserver) {
    TrafficConnectionKey connectionKey = generateConnectionKey();

    StreamObserver<TrafficSignalRes> wrappedObserver = new StreamObserver<>() {
      @Override
      public void onNext(TrafficSignalRes value) {
        responseObserver.onNext(value);
      }

      @Override
      public void onError(Throwable t) {
        unRegisterConnection(connectionKey);
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        unRegisterConnection(connectionKey);
        responseObserver.onCompleted();
      }
    };
    registerConnection(connectionKey, wrappedObserver);
    sendFirstSignal(connectionKey);
  }

  private void notifyClient(StreamObserver<TrafficSignalRes> streamObserver, TrafficSignalDto trafficSignalDto) {
    log.debug("Notifying client: {}", trafficSignalDto);
    try {
      streamObserver.onNext(
          TrafficSignalRes.newBuilder()
              .setCollectedUnixTime(trafficSignalDto.collectedUnixTime())
              .setARingMovementNumber(trafficSignalDto.aRingMovementNumber())
              .setBRingMovementNumber(trafficSignalDto.bRingMovementNumber())
              .build()
      );
    } catch (Exception e) {
      streamObserver.onError(e);
    }
  }

  public void notifyConnectedClients(List<TrafficSignalDto> trafficSignalDtos) {
    trafficSignalDtos.forEach(signal -> {
      if (connectionsByIntersectionId.containsKey(signal.spotIntersectionId())) {
        connectionsByIntersectionId.get(signal.spotIntersectionId())
            .forEach(connectionKey ->
              notifyClient(streamsByConnection.get(connectionKey), signal));
      }
    });
  }

  private TrafficConnectionKey generateConnectionKey() {
    String clientIp = clientIpProvider.getClientIp();
    String intersectionId = edgeService.getIntersectionIdByIp(clientIp);
    return TrafficConnectionKey.builder()
        .ip(clientIp)
        .spotIntersectionId(intersectionId)
        .build();
  }

  private void registerConnection(TrafficConnectionKey connectionKey, StreamObserver<TrafficSignalRes> responseObserver) {
    lock.lock();
    try {
      Set<TrafficConnectionKey> existingSet = connectionsByIntersectionId.computeIfAbsent(connectionKey.spotIntersectionId(),
          key -> ConcurrentHashMap.newKeySet());
      existingSet.add(connectionKey);
      streamsByConnection.put(connectionKey, responseObserver);
    } finally {
      lock.unlock();
    }
  }

  private void unRegisterConnection(TrafficConnectionKey connectionKey) {
    String intersectionId = connectionKey.spotIntersectionId();
    lock.lock();
    try {
      streamsByConnection.remove(connectionKey);
      if (connectionsByIntersectionId.containsKey(intersectionId)) {
        Set<TrafficConnectionKey> keys = connectionsByIntersectionId.get(intersectionId);
        keys.remove(connectionKey);
        if (keys.isEmpty()) {
          connectionsByIntersectionId.remove(intersectionId);
        }
      }
    } finally {
      lock.unlock();
    }

  }

  private void sendFirstSignal(TrafficConnectionKey connectionKey) {
    TrafficSignalDto trafficSignalDto = trafficSignalService.getDtoByIntersectionId(
        connectionKey.spotIntersectionId());
    notifyClient(streamsByConnection.get(connectionKey), trafficSignalDto);
  }
}
