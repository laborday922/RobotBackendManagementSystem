package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 点位创建请求DTO
 */
@ApiModel(description = "点位创建请求")
public class PointCreateRequest {

    @ApiModelProperty(value = "所属地图ID", required = true)
    @NotNull(message = "地图ID不能为空")
    private Long mapId;

    @ApiModelProperty(value = "点位名称", required = true)
    @NotBlank(message = "点位名称不能为空")
    private String pointName;

    @ApiModelProperty(value = "点位编码")
    private String pointCode;

    @ApiModelProperty(value = "点位类型", allowableValues = "normal,vip,service,exit")
    private String pointType = "normal";

    @ApiModelProperty(value = "X坐标")
    @DecimalMin(value = "-180", message = "X坐标范围-180~180")
    @DecimalMax(value = "180", message = "X坐标范围-180~180")
    private BigDecimal coordinateX;

    @ApiModelProperty(value = "Y坐标")
    @DecimalMin(value = "-90", message = "Y坐标范围-90~90")
    @DecimalMax(value = "90", message = "Y坐标范围-90~90")
    private BigDecimal coordinateY;

    @ApiModelProperty(value = "状态", allowableValues = "0,1")
    private String status = "1";

    @ApiModelProperty(value = "显示顺序")
    private Integer orderNum = 0;

    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getMapId() {
        return mapId;
    }

    public void setMapId(Long mapId) {
        this.mapId = mapId;
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

    public BigDecimal getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(BigDecimal coordinateX) {
        this.coordinateX = coordinateX;
    }

    public BigDecimal getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(BigDecimal coordinateY) {
        this.coordinateY = coordinateY;
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
}