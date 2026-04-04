package com.ruoyi.mode.service;

import com.ruoyi.mode.domain.SysRobotOperation;

import java.util.List;

/**
 * 机器人操作记录Service接口
 *
 * @author ruoyi
 */
public interface ISysRobotOperationService
{
    /**
     * 查询操作记录
     *
     * @param operationId 操作ID
     * @return 操作记录
     */
    public SysRobotOperation selectSysRobotOperationById(Long operationId);

    /**
     * 查询操作记录列表
     *
     * @param sysRobotOperation 操作记录
     * @return 操作记录集合
     */
    public List<SysRobotOperation> selectSysRobotOperationList(SysRobotOperation sysRobotOperation);

    /**
     * 新增操作记录
     *
     * @param sysRobotOperation 操作记录
     * @return 结果
     */
    public int insertSysRobotOperation(SysRobotOperation sysRobotOperation);

    /**
     * 批量删除操作记录
     *
     * @param operationIds 需要删除的操作记录ID
     * @return 结果
     */
    public int deleteSysRobotOperationByIds(Long[] operationIds);

    /**
     * 删除操作记录信息
     *
     * @param operationId 操作记录ID
     * @return 结果
     */
    public int deleteSysRobotOperationById(Long operationId);

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
    public int recordOperation(Long robotId, String robotName, String operationType,
                               String result, String operator, String remark);
}