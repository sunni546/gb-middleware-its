package com.middleware.edge.selfinfo.repository;

import java.util.Map;
import java.util.Set;

/**
 * Lane 정보에 접근하기 위한 Repository 인터페이스
 */
public interface LaneRepository {

  /**
   * 주어진 카메라 ID 집합에 대해, 각 카메라별 최소 차로 번호를 조회하는 메서드
   * <p>4K 장비에 대해서만 유효</p>
   *
   * @param cameraIds 조회 대상 카메라 ID 집합
   * @return 카메라 ID를 키로 하고 최소 차로 번호를 값으로 가지는 Map
   */
  Map<String, Integer> findMinLaneNoByCameraIds(Set<String> cameraIds);
}
