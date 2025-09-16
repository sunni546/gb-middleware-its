package com.middleware.trafficsignal.model;

import lombok.Builder;

@Builder
public record TrafficConnectionKey(String ip, String spotIntersectionId) { }
