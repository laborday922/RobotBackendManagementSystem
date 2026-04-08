package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysRobotOperation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机器人操作记录Mapper接口
 *
 * @author ruoyi
 */
public interface SysRobotOperationMapper
{

    public SysRobotOperation selectSysRobotOperationById(@Param("sysRobotOperation") SysRobotOperation sysRobotOperation);

    public List<SysRobotOperation> selectSysRobotOperationList(@Param("sysRobotOperation") SysRobotOperation sysRobotOperation);


    public int insertSysRobotOperation(SysRobotOperation sysRobotOperation);

    public int deleteSysRobotOperationById(Long operationId);


    public int deleteSysRobotOperationByIds(Long[] operationIds);
}