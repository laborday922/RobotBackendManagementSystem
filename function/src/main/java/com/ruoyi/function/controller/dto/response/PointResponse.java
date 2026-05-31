package com.ruoyi.function.controller.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 点位响应DTO
 */
@ApiModel(description = "点位响应")
@Getter
@Setter
public class PointResponse {

    @ApiModelProperty("系统点位ID")
    private Long sysPointId;

    @ApiModelProperty("机器人点位ID")
    private Long robotPointId;

    @ApiModelProperty("所属地图ID")
    private Long mapId;

    @ApiModelProperty("点位名称")
    private String pointName;

    @ApiModelProperty("点位编码")
    private String pointCode;

    @ApiModelProperty("点位类型")
    private String pointType;

    @ApiModelProperty("点位类型文本")
    private String pointTypeText;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("状态文本")
    private String statusText;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("备注")
    private String remark;
}
