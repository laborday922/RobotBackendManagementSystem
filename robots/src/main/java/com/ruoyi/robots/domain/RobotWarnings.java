package com.ruoyi.robots.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 机器人状态预警对象 robot_warnings
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class RobotWarnings extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预警ID */
    private Long id;

    /** 关联机器人ID（逻辑外键） */
    @Excel(name = "关联机器人ID", readConverterExp = "逻=辑外键")
    private Long robotId;

    /** 预警类型（0-低电量，1-硬件故障，2-硬件异常，3-离线） */
    @Excel(name = "预警类型", readConverterExp = "0=-低电量，1-硬件故障，2-硬件异常，3-离线")
    private String warningType;

    /** 预警描述内容 */
    @Excel(name = "预警描述内容")
    private String warningContent;

    /** 预警级别（0-提示，1-警告，2-错误） */
    @Excel(name = "预警级别", readConverterExp = "0=-提示，1-警告，2-错误")
    private String warningLevel;

    /** 预警状态（0-待处理，1-已解决，2-已忽略） */
    @Excel(name = "预警状态", readConverterExp = "0=-待处理，1-已解决，2-已忽略")
    private String status;

    /** 处理完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "处理完成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date resolveTime;

    /** 处理人 */
    @Excel(name = "处理人")
    private String resolveUser;

    /** 处理备注 */
    @Excel(name = "处理备注")
    private String resolveNote;

    /** 预警创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "预警创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    private Long tenantId;
}
