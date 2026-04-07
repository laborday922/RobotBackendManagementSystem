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

    @ApiModelProperty("地图Base64数据")
    private String mapBase64;

    @ApiModelProperty("地图URL（非数据库字段）")
    private String mapUrl;

    @ApiModelProperty("是否有图片")
    private Boolean hasImage;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("状态(0禁用 1启用)")
    private String status;

    // 非数据库字段，用于前端展示
    private Integer pointCountTemp;

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

    public Boolean getHasImage() { return hasImage; }
    public void setHasImage(Boolean hasImage) { this.hasImage = hasImage; }

    public Integer getPointCount() {
        return pointCount != null ? pointCount : pointCountTemp;
    }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getPointCountTemp() { return pointCountTemp; }
    public void setPointCountTemp(Integer pointCountTemp) { this.pointCountTemp = pointCountTemp; }
}