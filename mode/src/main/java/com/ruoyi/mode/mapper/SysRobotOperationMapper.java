package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysRobotOperation;

import java.util.List;

/**
 * 机器人操作记录Mapper接口
 *
 * @author ruoyi
 */
public interface SysRobotOperationMapper
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
     * 删除操作记录
     *
     * @param operationId 操作ID
     * @return 结果
     */
    public int deleteSysRobotOperationById(Long operationId);

    /**
     * 批量删除操作记录
     *
     * @param operationIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysRobotOperationByIds(Long[] operationIds);
}
