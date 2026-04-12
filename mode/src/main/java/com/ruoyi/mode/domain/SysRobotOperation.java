package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 机器人操作记录对象 sys_robot_operation
 *
 * @author ruoyi
 */
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

    // ==================== Getters and Setters ====================

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationResult() {
        return operationResult;
    }

    public void setOperationResult(String operationResult) {
        this.operationResult = operationResult;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

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