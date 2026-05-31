package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 机器人操作记录对象 sys_robot_operation
 *
 * @author ruoyi
 */
@Getter
@Setter
public class SysRobotOperation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 操作ID */
    private Long operationId;

    /** 机器人ID */
    private Long robotId;

    /** 机器人名称 */
    private String robotName;

    /** 操作类型 */
    private String operationType;

    /** 操作结果 */
    private String operationResult;

    /** 操作时间 */
    private Date operationTime;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;

    /** 租户ID */
    private Long tenantId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("operationId", getOperationId())
                .append("robotId", getRobotId())
                .append("robotName", getRobotName())
                .append("operationType", getOperationType())
                .append("operationResult", getOperationResult())
                .append("operationTime", getOperationTime())
                .append("operator", getOperator())
                .append("remark", getRemark())
                .append("tenantId", getTenantId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
