package com.middleware.trafficsignal.repository;

import com.middleware.trafficsignal.model.TrafficSignalDto;
import java.util.List;

public interface TrafficSignalRepository {
  List<TrafficSignalDto> findAllTrafficSignals();
}
