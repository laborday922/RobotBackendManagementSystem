package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 模式切换历史记录对象 sys_mode_history
 *
 * @author ruoyi
 */
@Getter
@Setter
public class SysModeHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 历史ID */
    private Long historyId;

    /** 操作时间 */
    private Date operationTime;

    /** 操作类型(mode-switch/config-change/alert/emergency) */
    private String operationType;

    /** 机器人ID */
    private Long robotId;

    /** 模式ID */
    private Long modeId;

    /** 操作内容 */
    private String content;

    /** 操作人 */
    private String operator;

    /** 状态(success/warning/danger) */
    private String status;

    /** 租户ID */
    private Long tenantId;

    // 非数据库字段
    private String robotName;
    private String modeName;

    private String beginTime;  // 开始时间
    private String endTime;    // 结束时间

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("historyId", getHistoryId())
                .append("operationTime", getOperationTime())
                .append("operationType", getOperationType())
                .append("robotId", getRobotId())
                .append("modeId", getModeId())
                .append("content", getContent())
                .append("operator", getOperator())
                .append("status", getStatus())
                .append("tenantId", getTenantId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .toString();
    }
}
