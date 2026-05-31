package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 业务接待配置对象 sys_reception_config
 */
@ApiModel(description = "业务接待配置实体")
@Getter
@Setter
public class SysReceptionConfig extends BaseEntity {

    @ApiModelProperty("配置ID")
    private Long configId;

    @ApiModelProperty("机器人ID")
    private Long robotId;

    @ApiModelProperty("首次进入欢迎语")
    private String welcome;

    @ApiModelProperty("重复唤醒播报语")
    private String repeat;

    @ApiModelProperty("未唤醒状态播报语")
    private String idle;

    @ApiModelProperty("VIP识别开启(0关闭 1开启)")
    private String vipEnabled;

    @ApiModelProperty("VIP识别语")
    private String vipGreeting;

    @ApiModelProperty("多VIP识别语")
    private String vipMulti;

    @ApiModelProperty("陌生人识别开启(0关闭 1开启)")
    private String strangerEnabled;

    @ApiModelProperty("陌生人识别语")
    private String strangerGreeting;

    @ApiModelProperty("多性别陌生人识别语")
    private String strangerMulti;

    /** 租户ID */
    private Long tenantId;
}
