package com.middleware.common.interceptor;

import com.middleware.grpc.exceptions.GrpcResponseException;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@GlobalServerInterceptor
public class GrpcGlobalExceptionHandlerInterceptor implements ServerInterceptor {

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next
  ) {
    ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

    return new SimpleForwardingServerCallListener<>(delegate) {
      @Override
      public void onHalfClose() {
        try {
          super.onHalfClose();
        } catch (Exception ex) {
          Status status = mapExceptionToStatus(ex);
          log.warn("Mapped exception to status: {} - {}", status.getCode(), ex.getMessage());
          call.close(status, new Metadata());
        }
      }
    };
  }

  private Status mapExceptionToStatus(Throwable ex) {
    if (ex instanceof GrpcResponseException grpcEx) {
      return Status.fromCode(grpcEx.getCode())
          .withDescription(grpcEx.getMessage());
    } else {
      return Status.INTERNAL.withDescription("Unexpected error occurred")
          .withCause(ex);
    }
  }
}