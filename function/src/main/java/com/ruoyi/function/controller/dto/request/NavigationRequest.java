package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

/**
 * 导航请求DTO
 */
@ApiModel(description = "导航请求")
public class NavigationRequest {

    @ApiModelProperty(value = "目标点位名称", required = true)
    @NotBlank(message = "目标点位名称不能为空")
    private String pointName;

    @ApiModelProperty(value = "起始点位名称（可选）")
    private String startPointName;

    @ApiModelProperty(value = "是否开启语音播报")
    private Boolean voiceEnabled = true;

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getStartPointName() {
        return startPointName;
    }

    public void setStartPointName(String startPointName) {
        this.startPointName = startPointName;
    }

    public Boolean getVoiceEnabled() {
        return voiceEnabled;
    }

    public void setVoiceEnabled(Boolean voiceEnabled) {
        this.voiceEnabled = voiceEnabled;
    }
}