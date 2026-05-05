package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * 点位更新请求DTO
 */
@ApiModel(description = "点位更新请求")
public class PointUpdateRequest {

    @ApiModelProperty(value = "点位ID", required = true)
    @NotNull(message = "点位ID不能为空")
    private Long pointId;

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

    @ApiModelProperty(value = "机器人位置ID")
    private Long robotPositionId;

    // Getters and Setters
    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
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