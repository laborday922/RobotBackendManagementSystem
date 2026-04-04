package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 地图对象 sys_map
 */
@ApiModel(description = "地图实体")
public class SysMap extends BaseEntity {

    @ApiModelProperty("地图ID")
    private Long mapId;

    @ApiModelProperty("地图名称")
    private String mapName;

    @ApiModelProperty("地图文件路径")
    private String mapFile;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("状态(0禁用 1启用)")
    private String status;

    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }

    public String getMapFile() { return mapFile; }
    public void setMapFile(String mapFile) { this.mapFile = mapFile; }

    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}