package com.ruoyi.function.controller.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("地图详情响应")
@Getter
@Setter
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
}
