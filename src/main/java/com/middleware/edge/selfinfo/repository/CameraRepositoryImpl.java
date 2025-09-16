package com.middleware.edge.selfinfo.repository;

import com.middleware.common.aop.SqlRetryOnFailure;
import com.middleware.common.config.JdbcTemplateConfig;
import com.middleware.edge.selfinfo.model.CameraDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public class CameraRepositoryImpl implements CameraRepository {
  private final JdbcTemplate jdbcTemplate;

  public CameraRepositoryImpl(@Qualifier(JdbcTemplateConfig.VOLT_DB_DEFAULT) JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SqlRetryOnFailure
  public List<CameraDto> findAllCameraInfo() {
    String sql =
        "SELECT EDGE_SYS_2K_IP, EDGE_SYS_4K_IP, "
            + "CAMR_2K_IP, CAMR_4K_IP, "
            + "SPOT_INTS_ID, SPOT_CAMR_ID, "
            + "ECU_INFO_TRSM_YN, INSTL_LOCN_NO "
        + "FROM SOITGCAMRINFO";

    return jdbcTemplate.query(sql,
        (rs, rowNum) -> CameraDto.builder()
            .edge2kIp(rs.getString("EDGE_SYS_2K_IP"))
            .edge4kIp(rs.getString("EDGE_SYS_4K_IP"))
            .camera2KIp(rs.getString("CAMR_2K_IP"))
            .camera4KIp(rs.getString("CAMR_4K_IP"))
            .spotIntersectionId(rs.getString("SPOT_INTS_ID"))
            .spotCameraId(rs.getString("SPOT_CAMR_ID"))
            .ecuInfoTransmissionYesOrNo(rs.getString("ECU_INFO_TRSM_YN"))
            .installedLocationNumber(rs.getString("INSTL_LOCN_NO"))
            .build()
    );
  }
}
