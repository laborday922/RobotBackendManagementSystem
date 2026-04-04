package com.ruoyi.robots.service.impl;

import java.util.List;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.robots.mapper.RobotsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.robots.mapper.RobotPositionHistoryMapper;
import com.ruoyi.robots.domain.RobotPositionHistory;
import com.ruoyi.robots.service.IRobotPositionHistoryService;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

/**
 * 机器人位置历史信息Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@Service
public class RobotPositionHistoryServiceImpl implements IRobotPositionHistoryService 
{
    @Autowired
    private RobotPositionHistoryMapper robotPositionHistoryMapper;
    @Autowired
    private RobotsMapper robotsMapper;

    /**
     * 查询机器人位置历史信息
     * 
     * @param id 机器人位置历史信息主键
     * @return 机器人位置历史信息
     */
    @Override  // 标记重写父类方法
    public RobotPositionHistory selectRobotPositionHistoryById(Long id)  // 方法声明：根据ID查询机器人位置历史信息
    {
        return robotPositionHistoryMapper.selectRobotPositionHistoryById(id);
    }

    /**
     * 查询机器人位置历史信息列表
     * 
     * @param robotPositionHistory 机器人位置历史信息
     * @return 机器人位置历史信息
     */
    @Override
    public List<RobotPositionHistory> selectRobotPositionHistoryList(RobotPositionHistory robotPositionHistory)
    {
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            robotPositionHistory.setTenantId(tenantId);
        return robotPositionHistoryMapper.selectRobotPositionHistoryList(robotPositionHistory);
    }

    /**
     * 新增机器人位置历史信息
     * 
     * @param robotPositionHistory 机器人位置历史信息
     * @return 结果
     */
    @Override
    public int insertRobotPositionHistory(RobotPositionHistory robotPositionHistory)
    {
        robotPositionHistory.setTenantId(TenantContext.get());
        return robotPositionHistoryMapper.insertRobotPositionHistory(robotPositionHistory);
    }

    /**
     * 修改机器人位置历史信息
     * 
     * @param robotPositionHistory 机器人位置历史信息
     * @return 结果
     */
    @Override
    public int updateRobotPositionHistory(RobotPositionHistory robotPositionHistory)
    {
        return robotPositionHistoryMapper.updateRobotPositionHistory(robotPositionHistory);
    }

    /**
     * 批量删除机器人位置历史信息
     * 
     * @param ids 需要删除的机器人位置历史信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotPositionHistoryByIds(Long[] ids)
    {
        return robotPositionHistoryMapper.deleteRobotPositionHistoryByIds(ids);
    }

    /**
     * 删除机器人位置历史信息信息
     * 
     * @param id 机器人位置历史信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotPositionHistoryById(Long id)
    {
        return robotPositionHistoryMapper.deleteRobotPositionHistoryById(id);
    }
}
