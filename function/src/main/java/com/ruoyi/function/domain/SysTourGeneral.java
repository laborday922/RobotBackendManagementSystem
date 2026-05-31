package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 讲解通用配置对象 sys_tour_general
 */
@ApiModel(description = "讲解通用配置实体")
@Getter
@Setter
public class SysTourGeneral extends BaseEntity {

    @ApiModelProperty("配置ID")
    private Long configId;

    @ApiModelProperty("机器人ID")
    private Long robotId;

    @ApiModelProperty("当前地图ID")
    private Long mapId;

    @ApiModelProperty("当前路线ID")
    private Long routeId;

    @ApiModelProperty("讲解音色")
    private String voice;

    @ApiModelProperty("语音交互(0关闭 1开启)")
    private String voiceInteraction;

    @ApiModelProperty("开始讲解口令")
    private String startCommand;

    @ApiModelProperty("运动前提示播报")
    private String beforeTip;

    @ApiModelProperty("讲解结束播报")
    private String endTip;

    @ApiModelProperty("结束后动作(stay停留/charge充电)")
    private String afterAction;

    /** 租户ID */
    private Long tenantId;
}
