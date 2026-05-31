package com.ruoyi.function.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 地图视图对象（前端展示用）
 */
@ApiModel(description = "地图视图对象")
@Getter
@Setter
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
}
