package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "地图实体")
public class SysMap extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("地图ID")
    private Long mapId;

    @ApiModelProperty("机器人ID")
    private String robotId;

    @ApiModelProperty("租户ID")
    private Long tenantId;

    @ApiModelProperty("地图名称")
    private String mapName;

    @ApiModelProperty("地图类型")
    private String mapType;

    @ApiModelProperty("地图路径")
    private String mapPath;

    @ApiModelProperty("缩略图")
    private String thumbnail;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("地图文件名")
    private String mapFile;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("版本")
    private String version;

    @ApiModelProperty("状态(0禁用 1启用)")
    private String status;

    @ApiModelProperty("是否启用")
    private Integer isEnable;

    @ApiModelProperty("删除标志(0正常 1删除)")
    private String delFlag;

    @ApiModelProperty("地图Base64数据")
    private String mapBase64;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否有图片（非数据库字段）")
    private Boolean hasImage;

    // ========== getter/setter ==========
    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public String getRobotId() { return robotId; }
    public void setRobotId(String robotId) { this.robotId = robotId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }

    public String getMapType() { return mapType; }
    public void setMapType(String mapType) { this.mapType = mapType; }

    public String getMapPath() { return mapPath; }
    public void setMapPath(String mapPath) { this.mapPath = mapPath; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMapFile() { return mapFile; }
    public void setMapFile(String mapFile) { this.mapFile = mapFile; }

    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getIsEnable() { return isEnable; }
    public void setIsEnable(Integer isEnable) { this.isEnable = isEnable; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getMapBase64() { return mapBase64; }
    public void setMapBase64(String mapBase64) { this.mapBase64 = mapBase64; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Boolean getHasImage() { return hasImage; }
    public void setHasImage(Boolean hasImage) { this.hasImage = hasImage; }

    @Override
    public String toString() {
        return "SysMap{" +
                "mapId=" + mapId +
                ", robotId='" + robotId + '\'' +
                ", tenantId=" + tenantId +
                ", mapName='" + mapName + '\'' +
                ", mapType='" + mapType + '\'' +
                ", mapPath='" + mapPath + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}