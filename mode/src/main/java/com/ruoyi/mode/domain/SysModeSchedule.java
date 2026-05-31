package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 模式排程对象 sys_mode_schedule
 *
 * @author ruoyi
 */
@Getter
@Setter
public class SysModeSchedule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 排程ID */
    private Long scheduleId;

    /** 排程名称 */
    private String scheduleName;

    /** 模式ID */
    private Long modeId;

    /** 模式名称 */
    private String modeName;

    /** 执行时间描述 */
    private String executeTime;

    /** 重复类型(once/daily/weekly/monthly/weekdays) */
    private String repeatType;

    /** 重复规则JSON */
    private String repeatRule;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /** 开始时间 */
    private String startTime;

    /** 持续时间(小时) */
    private BigDecimal duration;

    /** 状态(running/paused/pending/completed/failed) */
    private String status;

    /** 上次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastExecuteTime;

    /** 上次执行状态 */
    private String lastExecuteStatus;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 租户ID */
    private Long tenantId;

    /** 关联的机器人列表 */
    private List<SysRobot> robots;

    /** 机器人ID列表（用于接收前端数据） */
    private Long[] robotIds;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("scheduleId", getScheduleId())
                .append("scheduleName", getScheduleName())
                .append("modeId", getModeId())
                .append("modeName", getModeName())
                .append("executeTime", getExecuteTime())
                .append("repeatType", getRepeatType())
                .append("repeatRule", getRepeatRule())
                .append("startDate", getStartDate())
                .append("startTime", getStartTime())
                .append("duration", getDuration())
                .append("status", getStatus())
                .append("lastExecuteTime", getLastExecuteTime())
                .append("lastExecuteStatus", getLastExecuteStatus())
                .append("delFlag", getDelFlag())
                .append("tenantId", getTenantId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
