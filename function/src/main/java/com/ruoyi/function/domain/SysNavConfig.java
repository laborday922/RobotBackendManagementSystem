package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 导航配置对象 sys_nav_config
 */
@ApiModel(description = "导航配置实体")
@Getter
@Setter
public class SysNavConfig extends BaseEntity {

    @ApiModelProperty("导航ID")
    private Long navId;

    @ApiModelProperty("机器人ID")
    private String robotId;

    @ApiModelProperty("当前地图ID")
    private Long mapId;

    @ApiModelProperty("播报类型(default默认/custom自定义/none无)")
    private String voiceType;

    @ApiModelProperty("出发前播报")
    private String beforeMsg;

    @ApiModelProperty("导航中播报")
    private String duringMsg;

    @ApiModelProperty("到达后播报")
    private String afterMsg;

    /** 租户ID */
    private Long tenantId;
}
