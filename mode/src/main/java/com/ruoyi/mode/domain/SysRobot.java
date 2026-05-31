package com.ruoyi.mode.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 机器人模式扩展对象
 */
@Getter
@Setter
public class SysRobot implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 机器人ID（关联robots表） */
    private Long robotId;

    private String name;

    private String code;

    /** 组ID */
    private Long groupId;

    /** 状态 */
    private Integer status;

    /** 电池电量 */
    private Integer battery;

    /** 区域 */
    private String area;

    /** 当前模式ID */
    private Long currentMode;

    /** 当前模式名称（非数据库字段） */
    private String currentModeName;

    /** 最后模式切换时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModeSwitchTime;

    /** 模式切换次数 */
    private Integer modeSwitchCount;

    /** 是否需要自动充电（0-否，1-是） */
    private Integer needAutoCharge;

    /** 租户ID */
    private Long tenantId;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public String getRobotName() {
        return this.name;
    }

    public void setRobotName(String robotName) {
        this.name = robotName;
    }

    public String getRobotCode() {
        return this.code;
    }

    public void setRobotCode(String robotCode) {
        this.code = robotCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("robotId", getRobotId())
                .append("name", getName())
                .append("code", getCode())
                .append("groupId", getGroupId())
                .append("status", getStatus())
                .append("battery", getBattery())
                .append("area", getArea())
                .append("currentMode", getCurrentMode())
                .append("currentModeName", getCurrentModeName())
                .append("lastModeSwitchTime", getLastModeSwitchTime())
                .append("modeSwitchCount", getModeSwitchCount())
                .append("needAutoCharge", getNeedAutoCharge())
                .append("tenantId", getTenantId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
