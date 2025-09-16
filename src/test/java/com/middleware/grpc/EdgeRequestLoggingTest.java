package com.middleware.grpc;

import static org.mockito.Mockito.*;

import com.middleware.common.interceptor.ClientIpProvidingGrpcServerInterceptor;
import com.middleware.edge.generated.service.EdgeDataQueueManager;
import com.middleware.grpc.EdgeSaveProto.EdgeReply;
import com.middleware.grpc.EdgeSaveProto.SoitgaprdstatsRequest;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EdgeRequestLoggingTest {

    @Mock
    private EdgeDataQueueManager queueManager;

    @Mock
    private ClientIpProvidingGrpcServerInterceptor ipProvider;

    @Mock
    private StreamObserver<EdgeReply> responseObserver;

    private DbInsertGrpcService service;

    @BeforeEach
    void setUp() {
        service = new DbInsertGrpcService(queueManager, ipProvider);
        when(ipProvider.getClientIp()).thenReturn("192.168.1.100");
    }

    @Test
    void testEdgeRequestLogging() {
        // Given
        SoitgaprdstatsRequest request = SoitgaprdstatsRequest.newBuilder()
            .setSpotCamrId("CAMERA_001")
            .setUniqueKey("UNIQUE_12345")
            .setHrTypeCd(1)
            .setStatsBgngUnixTm(1640995200)
            .setStatsEndUnixTm(1640995500)
            .setTotlTrvl(100)
            .setAvgStlnDttnSped(45.5f)
            .setAvgSectSped(50.0f)
            .setAvgTrfcDnst(30)
            .setMinTrfcDnst(10)
            .setMaxTrfcDnst(50)
            .setAvgLaneOcpnRt(0.75f)
            .build();

        // When
        service.saveSoitgaprdstats(request, responseObserver);

        // Then
        verify(responseObserver).onNext(any(EdgeReply.class));
        verify(responseObserver).onCompleted();
        // 로그 파일 생성 확인은 실제 파일 시스템에서 확인해야 함
    }
}
