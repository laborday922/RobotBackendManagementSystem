package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 讲解路线对象 sys_tour_route
 */
@ApiModel(description = "讲解路线实体")
@Getter
@Setter
public class SysTourRoute extends BaseEntity {

    @ApiModelProperty("路线ID")
    private Long routeId;

    @ApiModelProperty("路线名称")
    private String routeName;

    @ApiModelProperty("机器人ID")
    private Long robotId;

    @ApiModelProperty("地图ID")
    private Long mapId;

    @ApiModelProperty("点位ID列表(JSON格式)")
    private String pointIds;

    @ApiModelProperty("点位数量")
    private Integer pointCount;

    @ApiModelProperty("状态(0禁用 1启用)")
    private String status;

    /** 租户ID */
    private Long tenantId;

    @ApiModelProperty("关联的点位列表（包含顺序和讲解内容）")
    private List<SysRoutePoint> routePoints;
}
