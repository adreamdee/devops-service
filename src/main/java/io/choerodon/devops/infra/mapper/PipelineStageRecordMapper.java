package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:13 2019/4/4
 * Description:
 */


public interface PipelineStageRecordMapper extends BaseMapper<PipelineStageRecordDTO> {
    List<PipelineStageRecordDTO> listByOptions(@Param("projectId") Long projectId,
                                          @Param("pipelineRecordId") Long pipelineRecordId);

    PipelineStageRecordDTO queryByPendingCheckStatus(@Param("pipelineRecordId") Long pipelineRecordId);
}
