package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 讲解内容对象 sys_tour_content
 */
@ApiModel(description = "讲解内容实体")
@Getter
@Setter
public class SysTourContent extends BaseEntity {

    @ApiModelProperty("内容ID")
    private Long contentId;

    @ApiModelProperty("机器人ID")
    private Long robotId;

    @ApiModelProperty("关联系统点位ID")
    private Long sysPointId;

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
}
