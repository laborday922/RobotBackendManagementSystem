package com.ruoyi.mode.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mode.domain.SysRobotOperation;
import com.ruoyi.mode.mapper.SysRobotOperationMapper;
import com.ruoyi.mode.service.ISysRobotOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

/**
 * 机器人操作记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysRobotOperationServiceImpl implements ISysRobotOperationService
{
    @Autowired
    private SysRobotOperationMapper sysRobotOperationMapper;

    /**
     * 查询操作记录
     *
     * @param operationId 操作ID
     * @return 操作记录
     */
    @Override
    public SysRobotOperation selectSysRobotOperationById(Long operationId)
    {
        return sysRobotOperationMapper.selectSysRobotOperationById(operationId);
    }

    /**
     * 查询操作记录列表
     *
     * @param sysRobotOperation 操作记录
     * @return 操作记录
     */
    @Override
    public List<SysRobotOperation> selectSysRobotOperationList(SysRobotOperation sysRobotOperation)
    {
        // 添加租户过滤
        Long tenantId = TenantContext.get();
        if (!isAdmin(tenantId)) {
            sysRobotOperation.setTenantId(tenantId);
        }
        return sysRobotOperationMapper.selectSysRobotOperationList(sysRobotOperation);
    }

    /**
     * 新增操作记录
     *
     * @param sysRobotOperation 操作记录
     * @return 结果
     */
    @Override
    public int insertSysRobotOperation(SysRobotOperation sysRobotOperation)
    {
        sysRobotOperation.setOperationTime(DateUtils.getNowDate());
        // 设置租户ID
        sysRobotOperation.setTenantId(TenantContext.get());
        return sysRobotOperationMapper.insertSysRobotOperation(sysRobotOperation);
    }

    /**
     * 批量删除操作记录
     *
     * @param operationIds 需要删除的操作记录ID
     * @return 结果
     */
    @Override
    public int deleteSysRobotOperationByIds(Long[] operationIds)
    {
        return sysRobotOperationMapper.deleteSysRobotOperationByIds(operationIds);
    }

    /**
     * 删除操作记录信息
     *
     * @param operationId 操作记录ID
     * @return 结果
     */
    @Override
    public int deleteSysRobotOperationById(Long operationId)
    {
        return sysRobotOperationMapper.deleteSysRobotOperationById(operationId);
    }

    /**
     * 记录机器人操作
     *
     * @param robotId 机器人ID
     * @param robotName 机器人名称
     * @param operationType 操作类型
     * @param result 操作结果
     * @param operator 操作人
     * @param remark 备注
     * @return 结果
     */
    @Override
    public int recordOperation(Long robotId, String robotName, String operationType,
                               String result, String operator, String remark)
    {
        SysRobotOperation operation = new SysRobotOperation();
        operation.setRobotId(robotId);
        operation.setRobotName(robotName);
        operation.setOperationType(operationType);
        operation.setOperationResult(result);
        operation.setOperationTime(DateUtils.getNowDate());
        operation.setOperator(operator);
        operation.setRemark(remark);
        operation.setTenantId(TenantContext.get());  // 设置租户ID
        return sysRobotOperationMapper.insertSysRobotOperation(operation);
    }
}