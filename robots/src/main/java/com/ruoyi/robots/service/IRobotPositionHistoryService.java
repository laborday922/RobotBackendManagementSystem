package com.ruoyi.robots.service;

import java.util.List;

import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.domain.RobotPositionHistory;

/**
 * 机器人位置历史信息Service接口
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
public interface IRobotPositionHistoryService 
{
    /**
     * 查询机器人位置历史信息
     * 
     * @param id 机器人位置历史信息主键
     * @return 机器人位置历史信息
     */
    public RobotPositionHistory selectRobotPositionHistoryById(Long id);

    /**
     * 查询机器人位置历史信息列表
     * 
     * @param robotPositionHistory 机器人位置历史信息
     * @return 机器人位置历史信息集合
     */
    public List<RobotPositionHistory> selectRobotPositionHistoryList(RobotPositionHistory robotPositionHistory);

    /**
     * 新增机器人位置历史信息
     * 
     * @param robotPositionHistory 机器人位置历史信息
     * @return 结果
     */
    public int insertRobotPositionHistory(RobotPositionHistory robotPositionHistory);

    /**
     * 修改机器人位置历史信息
     * 
     * @param robotPositionHistory 机器人位置历史信息
     * @return 结果
     */
    public int updateRobotPositionHistory(RobotPositionHistory robotPositionHistory);

    /**
     * 批量删除机器人位置历史信息
     * 
     * @param ids 需要删除的机器人位置历史信息主键集合
     * @return 结果
     */
    public int deleteRobotPositionHistoryByIds(Long[] ids);

    /**
     * 删除机器人位置历史信息信息
     * 
     * @param id 机器人位置历史信息主键
     * @return 结果
     */
    public int deleteRobotPositionHistoryById(Long id);

    void loadPosToRedis(RobotStatusDto robotStatusDto);
}
