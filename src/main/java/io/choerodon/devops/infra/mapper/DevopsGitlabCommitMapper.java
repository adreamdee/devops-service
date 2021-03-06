package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.mybatis.common.BaseMapper;



public interface DevopsGitlabCommitMapper extends BaseMapper<DevopsGitlabCommitDTO> {
    List<DevopsGitlabCommitDTO> listCommits(@Param("projectId") Long projectId,
                                            @Param("appServiceIds") List<Long> appServiceIds,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);


    List<DevopsGitlabCommitDTO> queryByAppIdAndBranch(
            @Param("appServiceId") Long appServiceId,
            @Param("branchName") String branchName,
            @Param("startDate") Date startDate);

    void deleteByAppServiceId(@Param("appServiceId") Long appServiceId);
}
