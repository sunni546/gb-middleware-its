package com.middleware.edge.selfinfo.repository;

import com.middleware.edge.selfinfo.model.CameraDto;
import java.util.List;

public interface CameraRepository {

  /**
   * VoltDB 149에서 모든 카메라 정보를 가져오는 메서드
   *
   * @return VoltDB에서 조회한 모든 카메라 정보 리스트
   */
  List<CameraDto> findAllCameraInfo();
}
