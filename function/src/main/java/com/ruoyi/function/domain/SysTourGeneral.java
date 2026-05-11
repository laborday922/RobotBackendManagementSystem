package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 讲解通用配置对象 sys_tour_general
 */
@ApiModel(description = "讲解通用配置实体")
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

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public Long getRobotId() { return robotId; }
    public void setRobotId(Long robotId) { this.robotId = robotId; }

    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getVoice() { return voice; }
    public void setVoice(String voice) { this.voice = voice; }

    public String getVoiceInteraction() { return voiceInteraction; }
    public void setVoiceInteraction(String voiceInteraction) { this.voiceInteraction = voiceInteraction; }

    public String getStartCommand() { return startCommand; }
    public void setStartCommand(String startCommand) { this.startCommand = startCommand; }

    public String getBeforeTip() { return beforeTip; }
    public void setBeforeTip(String beforeTip) { this.beforeTip = beforeTip; }

    public String getEndTip() { return endTip; }
    public void setEndTip(String endTip) { this.endTip = endTip; }

    public String getAfterAction() { return afterAction; }
    public void setAfterAction(String afterAction) { this.afterAction = afterAction; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}