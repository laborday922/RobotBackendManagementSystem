package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 点位更新请求DTO
 */
@ApiModel(description = "点位更新请求")
@Getter
@Setter
public class PointUpdateRequest {

    @ApiModelProperty(value = "系统点位ID", required = true)
    @NotNull(message = "系统点位ID不能为空")
    private Long sysPointId;

    @ApiModelProperty(value = "所属地图ID（可为空，为空时使用默认地图）")
    private Long mapId;

    @ApiModelProperty(value = "所属机器人ID")
    private Long robotId;

    @ApiModelProperty(value = "点位名称")
    private String pointName;

    @ApiModelProperty(value = "点位编码")
    private String pointCode;

    @ApiModelProperty(value = "点位类型")
    private String pointType;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "显示顺序")
    private Integer orderNum;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "机器人点位ID")
    private Long robotPointId;
}
