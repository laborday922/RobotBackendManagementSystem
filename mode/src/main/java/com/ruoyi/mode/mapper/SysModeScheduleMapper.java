package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.domain.SysRobot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模式排程Mapper接口
 *
 * @author ruoyi
 */
public interface SysModeScheduleMapper
{
    /**
     * 查询排程
     *
     * @param scheduleId 排程ID
     * @return 排程
     */
    public SysModeSchedule selectSysModeScheduleById(Long scheduleId);

    /**
     * 查询排程列表
     *
     * @param sysModeSchedule 排程
     * @return 排程集合
     */
    public List<SysModeSchedule> selectSysModeScheduleList(SysModeSchedule sysModeSchedule);

    /**
     * 新增排程
     *
     * @param sysModeSchedule 排程
     * @return 结果
     */
    public int insertSysModeSchedule(SysModeSchedule sysModeSchedule);

    /**
     * 修改排程
     *
     * @param sysModeSchedule 排程
     * @return 结果
     */
    public int updateSysModeSchedule(SysModeSchedule sysModeSchedule);

    /**
     * 删除排程（逻辑删除）
     *
     * @param scheduleId 排程ID
     * @return 结果
     */
    public int deleteSysModeScheduleById(Long scheduleId);

    /**
     * 批量删除排程（逻辑删除）
     *
     * @param scheduleIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysModeScheduleByIds(Long[] scheduleIds);

    /**
     * 更新排程状态
     *
     * @param scheduleId 排程ID
     * @param status 状态
     * @return 结果
     */
    public int updateScheduleStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);

    /**
     * 查询排程关联的机器人
     *
     * @param scheduleId 排程ID
     * @return 机器人列表
     */
    public List<SysRobot> selectRobotsByScheduleId(Long scheduleId);

    /**
     * 批量插入排程机器人关联
     *
     * @param scheduleId 排程ID
     * @param robotIds 机器人ID数组
     * @return 结果
     */
    public int insertScheduleRobots(@Param("scheduleId") Long scheduleId, @Param("robotIds") Long[] robotIds);

    /**
     * 删除排程机器人关联
     *
     * @param scheduleId 排程ID
     * @return 结果
     */
    public int deleteScheduleRobots(Long scheduleId);

    /**
     * 批量删除排程机器人关联
     *
     * @param scheduleIds 排程ID数组
     * @return 结果
     */
    public int deleteScheduleRobotsByScheduleIds(Long[] scheduleIds);
}