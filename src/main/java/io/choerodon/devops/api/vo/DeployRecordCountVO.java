package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈功能简述〉
 * 〈项目部署次数统计VO〉
 *
 * @author wanghao
 * @Date 2020/2/25 20:22
 */
public class DeployRecordCountVO {
    @ApiModelProperty("项目id")
    private Long id;
    @ApiModelProperty("每日部署次数")
    private List<Long> data = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }
}
