package com.middleware.common.interceptor;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@GlobalServerInterceptor
@Component
// 요청 IP를 추출해 Context에 저장하는 인터셉터
// ClientIpProvidingGrpcServerInterceptor.getClientIp()로 사용 가능
public class ClientIpProvidingGrpcServerInterceptor implements ServerInterceptor {

  private static final Context.Key<SocketAddress> CLIENT_ADDRESS = Context.key("client-address");

  public String getClientIp() {
    SocketAddress address = CLIENT_ADDRESS.get();
    if (address == null) {
      log.warn("Client address is not available in context");
      return "unknown";
    }

    String rawIp = address.toString();
    log.info("Raw client address: {}", rawIp);

    try {
      int startIndex = rawIp.indexOf("/");
      if (startIndex == -1) return rawIp;
      startIndex += 1;

      int endIndex = rawIp.indexOf(":", startIndex);
      if (endIndex == -1) return rawIp.substring(startIndex);
      if (startIndex >= endIndex) return rawIp;

      return rawIp.substring(startIndex, endIndex);
    } catch (StringIndexOutOfBoundsException e) {
      log.error("Failed to parse IP from address: {}", rawIp, e);
      return "unknown";
    }
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next
  ) {
    SocketAddress clientAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
    Context ctx = Context.current().withValue(CLIENT_ADDRESS, clientAddress);
    return Contexts.interceptCall(ctx, call, headers, next);
  }
}