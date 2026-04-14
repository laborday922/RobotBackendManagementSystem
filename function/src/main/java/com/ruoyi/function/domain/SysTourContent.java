package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 讲解内容对象 sys_tour_content
 */
@ApiModel(description = "讲解内容实体")
public class SysTourContent extends BaseEntity {

    @ApiModelProperty("内容ID")
    private Long contentId;

    @ApiModelProperty("机器人ID")
    private Long robotId;

    @ApiModelProperty("关联点位ID")
    private Long pointId;

    @ApiModelProperty("讲解点名称")
    private String pointName;

    @ApiModelProperty("讲解点描述")
    private String pointDesc;

    @ApiModelProperty("播报类型(text文本/audio音频)")
    private String broadcastType;

    @ApiModelProperty("播报内容")
    private String broadcastText;

    @ApiModelProperty("音频文件路径")
    private String audioFile;

    @ApiModelProperty("音色")
    private String voiceType;

    @ApiModelProperty("语速(%)")
    private Integer speechRate;

    @ApiModelProperty("间隔时间(毫秒)")
    private Integer intervalTime;

    @ApiModelProperty("内容类型(text/audio/image/video)")
    private String contentType;

    @ApiModelProperty("媒体文件路径")
    private String mediaFile;

    @ApiModelProperty("手臂动作")
    private String armAction;

    @ApiModelProperty("底盘转角")
    private Integer chassisAngle;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    /** 租户ID */
    private Long tenantId;

    // getters and setters
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }

    public Long getRobotId() { return robotId; }
    public void setRobotId(Long robotId) { this.robotId = robotId; }

    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }

    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }

    public String getPointDesc() { return pointDesc; }
    public void setPointDesc(String pointDesc) { this.pointDesc = pointDesc; }

    public String getBroadcastType() { return broadcastType; }
    public void setBroadcastType(String broadcastType) { this.broadcastType = broadcastType; }

    public String getBroadcastText() { return broadcastText; }
    public void setBroadcastText(String broadcastText) { this.broadcastText = broadcastText; }

    public String getAudioFile() { return audioFile; }
    public void setAudioFile(String audioFile) { this.audioFile = audioFile; }

    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }

    public Integer getSpeechRate() { return speechRate; }
    public void setSpeechRate(Integer speechRate) { this.speechRate = speechRate; }

    public Integer getIntervalTime() { return intervalTime; }
    public void setIntervalTime(Integer intervalTime) { this.intervalTime = intervalTime; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getMediaFile() { return mediaFile; }
    public void setMediaFile(String mediaFile) { this.mediaFile = mediaFile; }

    public String getArmAction() { return armAction; }
    public void setArmAction(String armAction) { this.armAction = armAction; }

    public Integer getChassisAngle() { return chassisAngle; }
    public void setChassisAngle(Integer chassisAngle) { this.chassisAngle = chassisAngle; }

    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}