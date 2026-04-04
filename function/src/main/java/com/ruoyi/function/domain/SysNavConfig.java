package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 导航配置对象 sys_nav_config
 */
@ApiModel(description = "导航配置实体")
public class SysNavConfig extends BaseEntity {

    @ApiModelProperty("导航ID")
    private Long navId;

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

    public Long getNavId() { return navId; }
    public void setNavId(Long navId) { this.navId = navId; }

    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }

    public String getBeforeMsg() { return beforeMsg; }
    public void setBeforeMsg(String beforeMsg) { this.beforeMsg = beforeMsg; }

    public String getDuringMsg() { return duringMsg; }
    public void setDuringMsg(String duringMsg) { this.duringMsg = duringMsg; }

    public String getAfterMsg() { return afterMsg; }
    public void setAfterMsg(String afterMsg) { this.afterMsg = afterMsg; }
}
