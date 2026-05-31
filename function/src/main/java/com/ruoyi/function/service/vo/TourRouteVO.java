package com.ruoyi.function.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 讲解路线视图对象
 */
@ApiModel(description = "讲解路线视图对象")
@Getter
@Setter
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
}
