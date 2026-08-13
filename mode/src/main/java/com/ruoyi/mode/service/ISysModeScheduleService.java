package com.ruoyi.mode.service;

import com.ruoyi.mode.domain.SysModeSchedule;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 模式排程Service接口
 *
 * @author ruoyi
 */
public interface ISysModeScheduleService
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
     * 批量删除排程
     *
     * @param scheduleIds 需要删除的排程ID
     * @return 结果
     */
    public int deleteSysModeScheduleByIds(Long[] scheduleIds);

    /**
     * 删除排程信息
     *
     * @param scheduleId 排程ID
     * @return 结果
     */
    public int deleteSysModeScheduleById(Long scheduleId);

    /**
     * 切换排程启用状态（启用/停用）
     *
     * @param scheduleId 排程ID
     * @return 结果
     */
    public int toggleScheduleStatus(Long scheduleId);

    /**
     * 获取日历数据
     *
     * @param year 年份
     * @param month 月份（可选）
     * @return 日历数据
     */
    public Map<String, Object> getCalendarData(Integer year, Integer month);

    // ==================== 定时任务用 ====================

    /**
     * 查询所有启用的排程（usable='1'，含关联机器人）
     */
    public List<SysModeSchedule> selectEnabledSchedules();

    /**
     * 更新排程状态（pending/running/paused/completed/failed）
     */
    public int updateScheduleStatus(Long scheduleId, String status);

    /**
     * 更新排程执行结果（记录上次执行时间和执行结果）
     */
    public int updateScheduleExecutionResult(Long scheduleId, String lastExecuteStatus, Date lastExecuteTime);
}