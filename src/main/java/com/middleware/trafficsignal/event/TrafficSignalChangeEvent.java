package com.middleware.trafficsignal.event;

import com.middleware.trafficsignal.model.TrafficSignalDto;
import java.util.List;

public record TrafficSignalChangeEvent(List<TrafficSignalDto> trafficSignalDtos) {}
