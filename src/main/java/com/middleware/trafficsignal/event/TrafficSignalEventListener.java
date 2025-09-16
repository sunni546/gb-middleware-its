package com.middleware.trafficsignal.event;

import com.middleware.grpc.TrafficSignalGrpcService;
import com.middleware.trafficsignal.model.TrafficSignalDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "traffic-signal.enabled", havingValue = "true", matchIfMissing = false)
public class TrafficSignalEventListener  {

  private final TrafficSignalGrpcService trafficSignalGrpcService;

  @EventListener
  public void onChange(TrafficSignalChangeEvent event) {
    List<TrafficSignalDto> trafficSignalDtos = event.trafficSignalDtos();
    trafficSignalGrpcService.notifyConnectedClients(trafficSignalDtos);
  }
}
