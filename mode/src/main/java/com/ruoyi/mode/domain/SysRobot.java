package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.util.Date;

/**
 * 机器人对象 robots
 *
 * @author ruoyi
 */
public class SysRobot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 机器人ID */
    private Long robotId;

    /** 机器人编码 */
    private String robotCode;

    /** 机器人名称 */
    private String robotName;

    /** 所属分组 */
    private Long groupId;

    /** 生产厂家 */
    private String manufacturer;

    /** 生产日期 */
    private Date productionDate;

    /** 所属区域 */
    private String area;

    /** 在线状态（0-离线，1-在线，2-待激活） */
    private Integer status;

    /** 硬件状态（0-正常，1-警告，2-故障） */
    private Integer hardwareStatus;

    /** 任务状态（0-执行中，1-充电中，2-闲置，3-维护） */
    private Integer taskStatus;

    /** 电量百分比 */
    private Long battery;

    /** 当前模式ID */
    private Long currentMode;

    /** 空闲开始时间 */
    private Date idleStartTime;

    /** 删除标志 */
    private String delFlag;

    // 以下字段保留用于兼容旧代码（不再使用，但保留getter/setter避免报错）
    private String runtime;
    private String speed;
    private String temperature;
    private Long tasksCompleted;

    // 非数据库字段
    private String currentModeName;

    // ==================== Getters and Setters ====================

    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public String getRobotCode() {
        return robotCode;
    }

    public void setRobotCode(String robotCode) {
        this.robotCode = robotCode;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getHardwareStatus() {
        return hardwareStatus;
    }

    public void setHardwareStatus(Integer hardwareStatus) {
        this.hardwareStatus = hardwareStatus;
    }

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Long getBattery() {
        return battery;
    }

    public void setBattery(Long battery) {
        this.battery = battery;
    }

    public Long getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(Long currentMode) {
        this.currentMode = currentMode;
    }

    public Date getIdleStartTime() {
        return idleStartTime;
    }

    public void setIdleStartTime(Date idleStartTime) {
        this.idleStartTime = idleStartTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    // 兼容旧代码的方法
    public String getLocation() {
        return area;
    }

    public void setLocation(String location) {
        this.area = location;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public Long getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(Long tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public String getCurrentModeName() {
        return currentModeName;
    }

    public void setCurrentModeName(String currentModeName) {
        this.currentModeName = currentModeName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("robotId", getRobotId())
                .append("robotCode", getRobotCode())
                .append("robotName", getRobotName())
                .append("groupId", getGroupId())
                .append("manufacturer", getManufacturer())
                .append("productionDate", getProductionDate())
                .append("area", getArea())
                .append("status", getStatus())
                .append("hardwareStatus", getHardwareStatus())
                .append("taskStatus", getTaskStatus())
                .append("battery", getBattery())
                .append("currentMode", getCurrentMode())
                .append("idleStartTime", getIdleStartTime())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}