package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 点位创建请求DTO
 */
@ApiModel(description = "点位创建请求")
@Getter
@Setter
public class PointCreateRequest {

    @ApiModelProperty(value = "机器人点位ID")
    private Long robotPointId;

    @ApiModelProperty(value = "所属地图ID（可为空，为空时使用默认地图）")
    private Long mapId;

    @ApiModelProperty(value = "所属机器人ID")
    private Long robotId;

    @ApiModelProperty(value = "点位名称", required = true)
    @NotBlank(message = "点位名称不能为空")
    private String pointName;

    @ApiModelProperty(value = "点位编码")
    private String pointCode;

    @ApiModelProperty(value = "点位类型", allowableValues = "normal,vip,service,exit")
    private String pointType = "normal";

    @ApiModelProperty(value = "状态", allowableValues = "0,1")
    private String status = "1";

    @ApiModelProperty(value = "显示顺序")
    private Integer orderNum = 0;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "机器人位置ID（从机器人获取的位置ID）")
    private Long robotPositionId;
}
