package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ProjectCertificationPermissionUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.DevopsProjectCertificationService;
import io.choerodon.swagger.annotation.CustomPageRequest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 泛域名证书
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/certs")
public class ProjectCertificationController {

    @Autowired
    private DevopsProjectCertificationService devopsProjectCertificationService;

    /**
     * 项目下创建或更新证书
     *
     * @param projectId              项目Id
     * @param projectCertificationVO 证书信息
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建或更新证书")
    @PostMapping
    public ResponseEntity createOrUpdate(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书信息", required = true)
            @ModelAttribute @Valid ProjectCertificationVO projectCertificationVO,
            BindingResult bindingResult,
            @ApiParam(value = "key文件")
            @RequestParam(value = "key", required = false) MultipartFile key,
            @ApiParam(value = "cert文件")
            @RequestParam(value = "cert", required = false) MultipartFile cert) {
        if (bindingResult.hasErrors()) {
            throw new CommonException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        devopsProjectCertificationService.createOrUpdate(projectId, key, cert, projectCertificationVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 查询单个证书信息
     *
     * @param projectId 项目Id
     * @param certId    证书Id
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询单个证书信息")
    @GetMapping("/{cert_id}")
    public ResponseEntity<ProjectCertificationVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        return Optional.ofNullable(devopsProjectCertificationService.queryCert(certId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.query"));
    }

    /**
     * 校验证书名唯一性
     *
     * @param projectId 项目id
     * @param name      证书name
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验证书名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书name", required = true)
            @RequestParam String name) {
        devopsProjectCertificationService.checkName(projectId, name);
    }

//    /**
//     * TODO 先注释，发版前删除
//     * 分页查询项目列表
//     *
//     * @param projectId 项目id
//     * @return Page
//     */
//    @Permission(type = ResourceType.PROJECT,
//            roles = {InitRoleCode.PROJECT_OWNER})
//    @ApiOperation(value = "分页查询项目列表")
//    @CustomPageRequest
//    @PostMapping("/page_projects")
//    public ResponseEntity<PageInfo<ProjectReqVO>> pageProjects(
//            @ApiParam(value = "项目ID", required = true)
//            @PathVariable(value = "project_id") Long projectId,
//            @ApiParam(value = "分页参数")
//            @ApiIgnore PageRequest pageRequest,
//            @ApiParam(value = "证书Id")
//            @RequestParam(required = false) Long certId,
//            @ApiParam(value = "模糊搜索参数")
//            @RequestBody String[] params) {
//        return Optional.ofNullable(devopsProjectCertificationService.pageProjects(projectId, certId, pageRequest, params))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException("error.project.query"));
//    }

//    /**
//     * TODO 先注释，发版前删除
//     * 查询已有权限的项目列表
//     *
//     * @param projectId 项目id
//     * @return List
//     */
//    @Permission(type = ResourceType.PROJECT,
//            roles = {InitRoleCode.PROJECT_OWNER})
//    @ApiOperation(value = "查询已有权限的项目列表")
//    @GetMapping("/list_cert_projects/{cert_id}")
//    public ResponseEntity<List<ProjectReqVO>> listCertProjects(
//            @ApiParam(value = "项目ID", required = true)
//            @PathVariable(value = "project_id") Long projectId,
//            @ApiParam(value = "证书Id")
//            @PathVariable(value = "cert_id") Long certId) {
//        return Optional.ofNullable(devopsProjectCertificationService.listCertProjects(certId))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException("error.project.query"));
//    }

    /**
     * 分页查询证书下已有权限的项目列表
     *
     * @param projectId   项目id
     * @param certId      证书id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询证书下已有权限的项目列表")
    @PostMapping("/{cert_id}/permission/page_related")
    public ResponseEntity<PageInfo<ProjectReqVO>> pageRelatedProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsProjectCertificationService.pageRelatedProjects(projectId, certId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }

//    /**
//     * TODO 发版前删除
//     * 查询已有权限的项目列表
//     *
//     * @param projectId 项目id
//     * @return List
//     */
//    @Permission(type = ResourceType.PROJECT,
//            roles = {InitRoleCode.PROJECT_OWNER})
//    @ApiOperation(value = "查询已有权限的项目列表")
//    @GetMapping("/list_cert_projects/{cert_id}")
//    public ResponseEntity<List<ProjectReqVO>> listCertProjects(
//            @ApiParam(value = "项目ID", required = true)
//            @PathVariable(value = "project_id") Long projectId,
//            @ApiParam(value = "证书Id")
//            @PathVariable(value = "cert_id") Long certId) {
//        return Optional.ofNullable(devopsProjectCertificationService.listCertProjects(certId))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException("error.project.query"));
//    }

    /**
     * 列出组织下所有项目中在数据库中没有权限关联关系的项目(不论当前数据库中是否跳过权限检查)
     *
     * @param projectId 项目ID
     * @param certId    证书ID
     * @param params    搜索参数
     * @return 所有与该证书未分配权限的项目
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "列出组织下所有项目中没有分配权限的项目")
    @PostMapping(value = "/{cert_id}/permission/list_non_related")
    public ResponseEntity<List<ProjectReqVO>> listAllNonRelatedMembers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书id", required = true)
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsProjectCertificationService.listNonRelatedMembers(projectId, certId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.cert.non.related.project"));
    }


    /**
     * 删除项目在该证书下的权限
     *
     * @param projectId 项目id
     * @param certId    证书id
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除项目在该证书下的权限")
    @DeleteMapping(value = "/{cert_id}/permission")
    public ResponseEntity deletePermissionOfProject(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书id", required = true)
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "关联的项目ID", required = true)
            @RequestParam(value = "related_project_id") Long relatedProjectId) {
        devopsProjectCertificationService.deletePermissionOfProject(relatedProjectId, certId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目证书列表查询
     *
     * @param projectId 项目ID
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目证书列表查询")
    @CustomPageRequest
    @PostMapping("/page_cert")
    public ResponseEntity<PageInfo<ProjectCertificationVO>> pageOrgCert(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsProjectCertificationService.pageCerts(projectId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.query"));
    }

    /**
     * 证书下为项目分配权限
     *
     * @param certId             证书id
     * @param permissionUpdateVO 权限分配信息
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "证书下为项目分配权限")
    @PostMapping(value = "/{cert_id}/permission")
    public ResponseEntity assignPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书id", required = true)
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "权限分配信息")
            @RequestBody @Valid ProjectCertificationPermissionUpdateVO permissionUpdateVO) {
        devopsProjectCertificationService.assignPermission(permissionUpdateVO);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 删除证书
     *
     * @param projectId 项目ID
     * @param certId    证书Id
     * @return String
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除证书")
    @CustomPageRequest
    @DeleteMapping("/{cert_id}")
    public ResponseEntity<String> deleteOrgCert(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        devopsProjectCertificationService.deleteCert(certId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}