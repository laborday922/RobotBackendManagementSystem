package com.ruoyi.function.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 点位对象 sys_point
 */
@ApiModel(description = "点位实体")
public class SysPoint extends BaseEntity {

    @ApiModelProperty("点位ID")
    private Long pointId;

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

    // ========== Getters and Setters ==========

    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }

    public Long getMapId() {
        return mapId;
    }

    public void setMapId(Long mapId) {
        this.mapId = mapId;
    }

    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public BigDecimal getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(BigDecimal coordinateX) {
        this.coordinateX = coordinateX;
    }

    public BigDecimal getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(BigDecimal coordinateY) {
        this.coordinateY = coordinateY;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getVoiceType() {
        return voiceType;
    }

    public void setVoiceType(String voiceType) {
        this.voiceType = voiceType;
    }

    public String getBeforeMsg() {
        return beforeMsg;
    }

    public void setBeforeMsg(String beforeMsg) {
        this.beforeMsg = beforeMsg;
    }

    public String getDuringMsg() {
        return duringMsg;
    }

    public void setDuringMsg(String duringMsg) {
        this.duringMsg = duringMsg;
    }

    public String getAfterMsg() {
        return afterMsg;
    }

    public void setAfterMsg(String afterMsg) {
        this.afterMsg = afterMsg;
    }

    @Override
    public String toString() {
        return "SysPoint{" +
                "pointId=" + pointId +
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