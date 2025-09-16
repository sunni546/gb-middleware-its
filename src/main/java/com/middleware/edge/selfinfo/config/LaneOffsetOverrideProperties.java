package com.middleware.edge.selfinfo.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * laneOffset 강제 오버라이드를 위한 설정 프로퍼티.
 *
 * application-laneoffset.yml:
 * edge:
 *   lane-offset-override:
 *     ip:
 *       "192.168.173.169": 3
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "edge.lane-offset-override")
public class LaneOffsetOverrideProperties {
  /**
   * key: ip 주소, value: laneOffset 강제값
   */
  private Map<String, Integer> ip = new HashMap<>();
}
