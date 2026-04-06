package com.ruoyi.function.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 地图视图对象（前端展示用）
 */
@ApiModel(description = "地图视图对象")
public class MapVO {

    @ApiModelProperty("地图ID")
    private Long mapId;

    @ApiModelProperty("地图名称")
    private String mapName;

    @ApiModelProperty("地图访问URL")
    private String mapUrl;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("状态标签（启用/禁用）")
    private String statusLabel;

    @ApiModelProperty("状态样式（success/danger/warning）")
    private String statusStyle;

    @ApiModelProperty("创建时间（格式化）")
    private String createTimeStr;

    public Long getMapId() {
        return mapId;
    }

    public void setMapId(Long mapId) {
        this.mapId = mapId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
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

    public String getStatusStyle() {
        return statusStyle;
    }

    public void setStatusStyle(String statusStyle) {
        this.statusStyle = statusStyle;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }
}