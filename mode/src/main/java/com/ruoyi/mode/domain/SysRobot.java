package com.ruoyi.mode.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 机器人模式扩展对象
 * 注意：基础信息请使用 Robot 类
 *
 * @author ruoyi
 */
public class SysRobot implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 机器人ID（关联robots表） */
    private Long robotId;

    private String name;           // 对应 name 字段

    private String code;           // 对应 code 字段

    /** 组ID */
    private Long groupId;          // 对应 group_id 字段

    /** 状态 */
    private Integer status;        // 对应 status 字段

    /** 电池电量 */
    private Integer battery;       // 对应 battery 字段

    /** 区域 */
    private String area;           // 对应 area 字段

    /** 当前模式ID */
    private Long currentMode;

    /** 当前模式名称（非数据库字段） */
    private String currentModeName;

    /** 最后模式切换时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModeSwitchTime;

    /** 模式切换次数 */
    private Integer modeSwitchCount;

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

    // ==================== Getters and Setters ====================

    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBattery() {
        return battery;
    }

    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Long getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(Long currentMode) {
        this.currentMode = currentMode;
    }

    public String getCurrentModeName() {
        return currentModeName;
    }

    public void setCurrentModeName(String currentModeName) {
        this.currentModeName = currentModeName;
    }

    public Date getLastModeSwitchTime() {
        return lastModeSwitchTime;
    }

    public void setLastModeSwitchTime(Date lastModeSwitchTime) {
        this.lastModeSwitchTime = lastModeSwitchTime;
    }

    public Integer getModeSwitchCount() {
        return modeSwitchCount;
    }

    public void setModeSwitchCount(Integer modeSwitchCount) {
        this.modeSwitchCount = modeSwitchCount;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    // 兼容前端的 robotName 和 robotCode
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
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}