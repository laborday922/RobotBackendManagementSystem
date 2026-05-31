package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "地图实体")
@Getter
@Setter
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
