package com.ruoyi.function.controller.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 导航响应DTO
 */
@ApiModel(description = "导航响应")
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

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTargetPoint() {
        return targetPoint;
    }

    public void setTargetPoint(String targetPoint) {
        this.targetPoint = targetPoint;
    }

    public Double getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(Double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getNavigationId() {
        return navigationId;
    }

    public void setNavigationId(String navigationId) {
        this.navigationId = navigationId;
    }
}