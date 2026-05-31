package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 点位对象 sys_point
 */
@ApiModel(description = "点位实体")
@Getter
@Setter
public class SysPoint extends BaseEntity {

    @ApiModelProperty("系统点位ID")
    private Long sysPointId;

    @ApiModelProperty("机器人点位ID")
    private Long robotPointId;

    @ApiModelProperty("所属地图ID")
    private Long mapId;

    @ApiModelProperty("所属机器人ID")
    private Long robotId;

    @ApiModelProperty("点位名称")
    private String pointName;

    @ApiModelProperty("点位编码")
    private String pointCode;

    @ApiModelProperty("点位类型(normal/vip/service/exit)")
    private String pointType;

    @ApiModelProperty("X坐标")
    private BigDecimal coordinateX;

    @ApiModelProperty("Y坐标")
    private BigDecimal coordinateY;

    @ApiModelProperty("状态(0禁用 1启用)")
    private String status;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    @ApiModelProperty("删除标志(0存在 2删除)")
    private String delFlag;

    /** 租户ID */
    private Long tenantId;

    /** 播报类型(default-全局配置/custom-自定义/none-无播报) */
    @ApiModelProperty("播报类型(default/custom/none)")
    private String voiceType;

    /** 出发前播报 */
    @ApiModelProperty("出发前播报")
    private String beforeMsg;

    /** 导航中播报 */
    @ApiModelProperty("导航中播报")
    private String duringMsg;

    /** 到达后播报 */
    @ApiModelProperty("到达后播报")
    private String afterMsg;

    @Override
    public String toString() {
        return "SysPoint{" +
                "sysPointId=" + sysPointId +
                ", robotPointId=" + robotPointId +
                ", mapId=" + mapId +
                ", robotId=" + robotId +
                ", pointName='" + pointName + '\'' +
                ", pointCode='" + pointCode + '\'' +
                ", pointType='" + pointType + '\'' +
                ", coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY +
                ", status='" + status + '\'' +
                ", orderNum=" + orderNum +
                ", delFlag='" + delFlag + '\'' +
                ", tenantId=" + tenantId +
                ", voiceType='" + voiceType + '\'' +
                ", beforeMsg='" + beforeMsg + '\'' +
                ", duringMsg='" + duringMsg + '\'' +
                ", afterMsg='" + afterMsg + '\'' +
                ", createBy='" + getCreateBy() + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateBy='" + getUpdateBy() + '\'' +
                ", updateTime=" + getUpdateTime() +
                ", remark='" + getRemark() + '\'' +
                '}';
    }
}
