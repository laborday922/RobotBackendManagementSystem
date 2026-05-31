package com.ruoyi.function.controller.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 导航响应DTO
 */
@ApiModel(description = "导航响应")
@Getter
@Setter
public class NavigationResponse {

    @ApiModelProperty("导航状态")
    private Boolean success;

    @ApiModelProperty("消息")
    private String message;

    @ApiModelProperty("目标点位")
    private String targetPoint;

    @ApiModelProperty("预计距离（米）")
    private Double estimatedDistance;

    @ApiModelProperty("预计时间（秒）")
    private Integer estimatedTime;

    @ApiModelProperty("导航ID")
    private String navigationId;

    public NavigationResponse() {}

    public NavigationResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static NavigationResponse success(String targetPoint) {
        NavigationResponse response = new NavigationResponse(true, "导航开始");
        response.setTargetPoint(targetPoint);
        return response;
    }

    public static NavigationResponse error(String message) {
        return new NavigationResponse(false, message);
    }
}
