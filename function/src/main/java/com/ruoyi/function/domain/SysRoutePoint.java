package com.ruoyi.function.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 路线点位关联对象 sys_route_point
 */
@ApiModel(description = "路线点位关联实体")
@Getter
@Setter
public class SysRoutePoint {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("路线ID")
    private Long routeId;

    @ApiModelProperty("系统点位ID")
    private Long sysPointId;

    @ApiModelProperty("关联讲解内容ID")
    private Long contentId;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    @ApiModelProperty("点位信息")
    private SysPoint point;

    @ApiModelProperty("讲解内容信息")
    private SysTourContent content;
}
