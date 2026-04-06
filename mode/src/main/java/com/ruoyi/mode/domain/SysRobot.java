package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.*;
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
    @NotBlank(message = "机器人编码不能为空")
    @Size(min = 1, max = 50, message = "机器人编码长度必须在1-50之间")
    private String robotCode;

    /** 机器人名称 */
    @NotBlank(message = "机器人名称不能为空")
    @Size(min = 1, max = 100, message = "机器人名称长度必须在1-100之间")
    private String robotName;

    /** 所属分组 */
    private Long groupId;

    /** 生产厂家 */
    @Size(max = 200, message = "生产厂家名称长度不能超过200")
    private String manufacturer;

    /** 生产日期 */
    private Date productionDate;

    /** 所属区域 */
    @Size(max = 100, message = "区域名称长度不能超过100")
    private String area;

    /** 在线状态（0-离线，1-在线，2-待激活） */
    @Min(value = 0, message = "状态值无效")
    @Max(value = 2, message = "状态值无效")
    private Integer status;

    /** 硬件状态（0-正常，1-警告，2-故障） */
    @Min(value = 0, message = "硬件状态值无效")
    @Max(value = 2, message = "硬件状态值无效")
    private Integer hardwareStatus;

    /** 任务状态（0-执行中，1-充电中，2-闲置，3-维护） */
    @Min(value = 0, message = "任务状态值无效")
    @Max(value = 3, message = "任务状态值无效")
    private Integer taskStatus;

    /** 电量百分比 */
    @Min(value = 0, message = "电量不能小于0")
    @Max(value = 100, message = "电量不能大于100")
    private Long battery;

    /** 当前模式ID */
    private Long currentMode;

    /** 空闲开始时间 */
    private Date idleStartTime;

    /** 删除标志 */
    private String delFlag;

    // 以下字段保留用于兼容旧代码
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