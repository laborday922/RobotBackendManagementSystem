package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 导航请求DTO
 */
@ApiModel(description = "导航请求")
@Getter
@Setter
public class NavigationRequest {

    @ApiModelProperty(value = "目标点位名称", required = true)
    @NotBlank(message = "目标点位名称不能为空")
    private String pointName;

    @ApiModelProperty(value = "起始点位名称（可选）")
    private String startPointName;

    @ApiModelProperty(value = "是否开启语音播报")
    private Boolean voiceEnabled = true;
}
