package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:57
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CiConfigTemplateVO {
    @ApiModelProperty("步骤名称")
    @NotEmpty(message = "error.step.name.cannot.be.null")
    private String name;

    @NotEmpty(message = "error.step.type.cannot.be.empty")
    @ApiModelProperty("步骤类型")
    private String type;

    @ApiModelProperty("步骤顺序")
    @NotNull(message = "error.step.sequence.cannot.be.null")
    private Long sequence;

    @ApiModelProperty("执行脚本")
    private String script;

    @ApiModelProperty("表单填写的Maven的依赖仓库")
    private List<MavenRepoVO> repos;

    @ApiModelProperty("直接粘贴的maven的settings内容")
    private String mavenSettings;

    @ApiModelProperty("Docker步骤的构建上下文")
    private String dockerContextDir;

    @ApiModelProperty("Dockerfile文件路径")
    private String dockerFilePath;

    @ApiModelProperty("上传软件包的文件路径")
    private String uploadFilePattern;

    @ApiModelProperty("上传或下载软件包的名称")
    private String artifactFileName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public List<MavenRepoVO> getRepos() {
        return repos;
    }

    public void setRepos(List<MavenRepoVO> repos) {
        this.repos = repos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDockerContextDir() {
        return dockerContextDir;
    }

    public void setDockerContextDir(String dockerContextDir) {
        this.dockerContextDir = dockerContextDir;
    }

    public String getDockerFilePath() {
        return dockerFilePath;
    }

    public void setDockerFilePath(String dockerFilePath) {
        this.dockerFilePath = dockerFilePath;
    }

    public String getUploadFilePattern() {
        return uploadFilePattern;
    }

    public void setUploadFilePattern(String uploadFilePattern) {
        this.uploadFilePattern = uploadFilePattern;
    }

    public String getArtifactFileName() {
        return artifactFileName;
    }

    public void setArtifactFileName(String artifactFileName) {
        this.artifactFileName = artifactFileName;
    }

    public String getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = mavenSettings;
    }
}
