package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1PersistentVolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvReqVo;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.util.GitOpsUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author zmf
 * @since 11/7/19
 */
@Service
public class HandlerPersistentVolumeServiceImpl implements HandlerObjectFileRelationsService<V1PersistentVolume> {
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsPvServcie devopsPvService;
    @Autowired
    private DevopsPvMapper devopsPvMapper;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<V1PersistentVolume> pvs, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforePvs = beforeSync.stream()
                .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(ResourceType.PERSISTENT_VOLUME.getType()))
                .map(devopsEnvFileResourceDTO -> {
                    DevopsPvDTO devopsPvDTO = devopsPvMapper
                            .selectByPrimaryKey(devopsEnvFileResourceDTO.getResourceId());
                    if (devopsPvDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceDTO.getResourceId(), ResourceType.PERSISTENT_VOLUME.getType());
                        return null;
                    }
                    return devopsPvDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<V1PersistentVolume> pvToAdd = new ArrayList<>();
        List<V1PersistentVolume> pvToUpdate = new ArrayList<>();

        GitOpsUtil.pickCUDResource(beforePvs, pvs, pvToAdd, pvToUpdate, pv -> pv.getMetadata().getName());

        //新增pv
        addPersistentVolumes(objectPath, envId, pvToAdd, path, userId);
        //更新pv
        updatePersistentVolumes(objectPath, envId, pvToUpdate, path, userId);
        //删除pv,和文件对象关联关系
        beforePvs.forEach(pvName -> {
            DevopsPvDTO devopsPvDTO = devopsPvService.queryByEnvIdAndName(envId, pvName);
            if (devopsPvDTO != null) {
                devopsPvService.deleteByGitOps(devopsPvDTO.getId());
                devopsEnvFileResourceService
                        .baseDeleteByEnvIdAndResourceId(envId, devopsPvDTO.getId(), ResourceType.PERSISTENT_VOLUME.getType());
            }
        });
    }

    private void updatePersistentVolumes(Map<String, String> objectPath, Long envId, List<V1PersistentVolume> updatePvs, String path, Long userId) {
        updatePvs.forEach(pv -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
                DevopsPvDTO devopsPvDTO = devopsPvService
                        .queryByEnvIdAndName(envId, pv.getMetadata().getName());
                //初始化pv对象参数,更新pv并更新文件对象关联关系
                DevopsPvReqVo devopsPvReqVo = constructPv(
                        pv,
                        envId, "update");
                boolean isNotChange = isIdentical(devopsPvDTO, devopsPvReqVo);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvDTO.getCommandId());
                devopsPvReqVo.setId(devopsPvDTO.getId());
                if (!isNotChange) {
                    devopsPvService.createOrUpdateByGitOps(devopsPvReqVo, userId);
                    DevopsPvDTO newDevOpsPvDTO = devopsPvService
                            .queryByEnvIdAndName(envId, pv.getMetadata().getName());
                    devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevOpsPvDTO.getCommandId());
                }
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsPvDTO.getId(), pv.getKind());
                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                        envId,
                        devopsEnvFileResourceDTO,
                        pv.hashCode(), devopsPvDTO.getId(), pv.getKind());

            } catch (CommonException e) {
                String errorCode = "";
                if (e instanceof GitOpsExplainException) {
                    errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                }
                throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
            }
        });
    }

    private boolean isIdentical(DevopsPvDTO dbRecord, DevopsPvReqVo update) {
        return Objects.equals(dbRecord.getAccessModes(), update.getAccessModes())
                && Objects.equals(dbRecord.getRequestResource(), update.getRequestResource())
                && Objects.equals(dbRecord.getType(), update.getType());
        // TODO 比较配置
    }

    private void addPersistentVolumes(Map<String, String> objectPath, Long envId, List<V1PersistentVolume> pvs, String path, Long userId) {
        pvs.forEach(pv -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
                DevopsPvDTO devopsPvDTO = devopsPvService
                        .queryByEnvIdAndName(envId, pv.getMetadata().getName());
                DevopsPvReqVo devopsPvReqVo;

                DevopsPvDTO newDevopsPvDTO = new DevopsPvDTO();
                //初始化pv参数,创建时判断pv是否存在，存在则直接创建文件对象关联关系
                if (devopsPvDTO == null) {
                    devopsPvReqVo = constructPv(
                            pv,
                            envId,
                            "create");
                    newDevopsPvDTO = devopsPvService.createOrUpdateByGitOps(devopsPvReqVo, userId);
                } else {
                    newDevopsPvDTO.setId(devopsPvDTO.getId());
                    newDevopsPvDTO.setCommandId(devopsPvDTO.getCommandId());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsPvDTO.getCommandId());
                // TODO 考虑并发
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, pv.hashCode(), newDevopsPvDTO.getId(),
                        pv.getKind());
            } catch (CommonException e) {
                String errorCode = "";
                if (e instanceof GitOpsExplainException) {
                    errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                }
                throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
            }
        });
    }


    private DevopsPvReqVo constructPv(V1PersistentVolume pv, Long envId, String type) {
        DevopsPvReqVo devopsPvReqVo = new DevopsPvReqVo();
        devopsPvReqVo.setEnvId(envId);
        devopsPvReqVo.setName(pv.getMetadata().getName());
        devopsPvReqVo.setCommandType(type);
        // 暂时只设计为支持一种模式
        devopsPvReqVo.setAccessModes(pv.getSpec().getAccessModes().get(0));
        devopsPvReqVo.setRequestResource(pv.getSpec().getCapacity().get(KubernetesConstants.STORAGE).toSuffixedString());
        setTypeAndConfig(devopsPvReqVo);
        return devopsPvReqVo;
    }

    private void setTypeAndConfig(DevopsPvReqVo devopsPvReqVo) {
        // TODO by zmf
    }

    @Override
    public Class<V1PersistentVolume> getTarget() {
        return V1PersistentVolume.class;
    }
}