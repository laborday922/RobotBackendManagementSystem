package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 业务接待配置对象 sys_reception_config
 */
@ApiModel(description = "业务接待配置实体")
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

    // getters and setters
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public Long getRobotId() { return robotId; }
    public void setRobotId(Long robotId) { this.robotId = robotId; }

    public String getWelcome() { return welcome; }
    public void setWelcome(String welcome) { this.welcome = welcome; }

    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }

    public String getIdle() { return idle; }
    public void setIdle(String idle) { this.idle = idle; }

    public String getVipEnabled() { return vipEnabled; }
    public void setVipEnabled(String vipEnabled) { this.vipEnabled = vipEnabled; }

    public String getVipGreeting() { return vipGreeting; }
    public void setVipGreeting(String vipGreeting) { this.vipGreeting = vipGreeting; }

    public String getVipMulti() { return vipMulti; }
    public void setVipMulti(String vipMulti) { this.vipMulti = vipMulti; }

    public String getStrangerEnabled() { return strangerEnabled; }
    public void setStrangerEnabled(String strangerEnabled) { this.strangerEnabled = strangerEnabled; }

    public String getStrangerGreeting() { return strangerGreeting; }
    public void setStrangerGreeting(String strangerGreeting) { this.strangerGreeting = strangerGreeting; }

    public String getStrangerMulti() { return strangerMulti; }
    public void setStrangerMulti(String strangerMulti) { this.strangerMulti = strangerMulti; }
}