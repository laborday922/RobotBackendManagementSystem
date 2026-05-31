package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * 点位更新请求DTO
 */
@ApiModel(description = "点位更新请求")
public class PointUpdateRequest {

    @ApiModelProperty(value = "系统点位ID", required = true)
    @NotNull(message = "系统点位ID不能为空")
    private Long sysPointId;

    @ApiModelProperty(value = "所属地图ID（可为空，为空时使用默认地图）")
    private Long mapId;

    @ApiModelProperty(value = "所属机器人ID")
    private Long robotId;

    @ApiModelProperty(value = "点位名称")
    private String pointName;

    @ApiModelProperty(value = "点位编码")
    private String pointCode;

    @ApiModelProperty(value = "点位类型")
    private String pointType;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "显示顺序")
    private Integer orderNum;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "机器人点位ID")
    private Long robotPointId;

    // Getters and Setters
    public Long getSysPointId() {
        return sysPointId;
    }

    public void setSysPointId(Long sysPointId) {
        this.sysPointId = sysPointId;
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

    public Long getRobotPointId() {
        return robotPointId;
    }

    public void setRobotPointId(Long robotPointId) {
        this.robotPointId = robotPointId;
    }
}
