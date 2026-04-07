package com.ruoyi.function.controller.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("地图详情响应")
public class MapDetailResponse {

    @ApiModelProperty("地图ID")
    private Long mapId;

    @ApiModelProperty("地图名称")
    private String mapName;

    @ApiModelProperty("地图文件路径")
    private String mapFile;

    @ApiModelProperty("地图Base64数据")
    private String mapBase64;

    @ApiModelProperty("地图访问URL")
    private String mapUrl;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("状态文本")
    private String statusText;

    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }

    public String getMapFile() { return mapFile; }
    public void setMapFile(String mapFile) { this.mapFile = mapFile; }

    public String getMapBase64() { return mapBase64; }
    public void setMapBase64(String mapBase64) { this.mapBase64 = mapBase64; }

    public String getMapUrl() { return mapUrl; }
    public void setMapUrl(String mapUrl) { this.mapUrl = mapUrl; }

    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
}