package com.ruoyi.function.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 讲解路线视图对象
 */
@ApiModel(description = "讲解路线视图对象")
public class TourRouteVO {

    @ApiModelProperty("路线ID")
    private Long routeId;

    @ApiModelProperty("路线名称")
    private String routeName;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("状态标签")
    private String statusLabel;

    @ApiModelProperty("点位列表（简要信息）")
    private List<PointVO> pointList;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Integer getPointCount() {
        return pointCount;
    }

    public void setPointCount(Integer pointCount) {
        this.pointCount = pointCount;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public List<PointVO> getPointList() {
        return pointList;
    }

    public void setPointList(List<PointVO> pointList) {
        this.pointList = pointList;
    }
}