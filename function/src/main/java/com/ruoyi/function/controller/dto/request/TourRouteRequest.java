package com.ruoyi.function.controller.dto.request;

import com.ruoyi.function.domain.SysRoutePoint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 讲解路线请求DTO
 */
@ApiModel(description = "讲解路线请求")
@Getter
@Setter
public class TourRouteRequest {

    @ApiModelProperty(value = "路线ID（更新时传入）")
    private Long routeId;

    @ApiModelProperty(value = "路线名称", required = true)
    @NotBlank(message = "路线名称不能为空")
    private String routeName;

    @ApiModelProperty(value = "地图ID", required = true)
    @NotNull(message = "地图ID不能为空")
    private Long mapId;

    @ApiModelProperty(value = "状态")
    private String status = "1";

    @ApiModelProperty(value = "路线点位列表")
    private List<SysRoutePoint> routePoints;
}
