package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

/**
 * 点位创建请求DTO
 */
@ApiModel(description = "点位创建请求")
public class PointCreateRequest {

    @ApiModelProperty(value = "点位ID（从机器人获取的位置ID，直接作为point_id存储）")
    private Long pointId;

    @ApiModelProperty(value = "所属地图ID（可为空，为空时使用默认地图）")
    private Long mapId;

    @ApiModelProperty(value = "所属机器人ID")
    private Long robotId;

    @ApiModelProperty(value = "点位名称", required = true)
    @NotBlank(message = "点位名称不能为空")
    private String pointName;

    @ApiModelProperty(value = "点位编码")
    private String pointCode;

    @ApiModelProperty(value = "点位类型", allowableValues = "normal,vip,service,exit")
    private String pointType = "normal";

    @ApiModelProperty(value = "状态", allowableValues = "0,1")
    private String status = "1";

    @ApiModelProperty(value = "显示顺序")
    private Integer orderNum = 0;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "机器人位置ID（从机器人获取的位置ID）")
    private Long robotPositionId;

    // Getters and Setters
    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }

    public Long getMapId() {
        return mapId;
    }

    public void setMapId(Long mapId) {
        this.mapId = mapId;
    }

    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getRobotPositionId() {
        return robotPositionId;
    }

    public void setRobotPositionId(Long robotPositionId) {
        this.robotPositionId = robotPositionId;
    }
}