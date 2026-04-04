package com.ruoyi.function.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 路线点位关联对象 sys_route_point
 */
@ApiModel(description = "路线点位关联实体")
public class SysRoutePoint {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("路线ID")
    private Long routeId;

    @ApiModelProperty("点位ID")
    private Long pointId;

    @ApiModelProperty("关联讲解内容ID")
    private Long contentId;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    @ApiModelProperty("点位信息")
    private SysPoint point;

    @ApiModelProperty("讲解内容信息")
    private SysTourContent content;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }

    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }

    public SysPoint getPoint() { return point; }
    public void setPoint(SysPoint point) { this.point = point; }

    public SysTourContent getContent() { return content; }
    public void setContent(SysTourContent content) { this.content = content; }
}