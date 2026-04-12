package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 讲解路线对象 sys_tour_route
 */
@ApiModel(description = "讲解路线实体")
public class SysTourRoute extends BaseEntity {

    @ApiModelProperty("路线ID")
    private Long routeId;

    @ApiModelProperty("路线名称")
    private String routeName;

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

    @ApiModelProperty("关联的点位列表")
    private List<SysRoutePoint> routePoints;

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public String getPointIds() { return pointIds; }
    public void setPointIds(String pointIds) { this.pointIds = pointIds; }

    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public List<SysRoutePoint> getRoutePoints() { return routePoints; }
    public void setRoutePoints(List<SysRoutePoint> routePoints) { this.routePoints = routePoints; }
}