package com.ruoyi.mode.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.mapper.SysModeScheduleMapper;
import com.ruoyi.mode.service.ISysModeScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模式排程Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysModeScheduleServiceImpl implements ISysModeScheduleService
{
    @Autowired
    private SysModeScheduleMapper sysModeScheduleMapper;

    /**
     * 查询排程
     *
     * @param scheduleId 排程ID
     * @return 排程
     */
    @Override
    public SysModeSchedule selectSysModeScheduleById(Long scheduleId)
    {
        SysModeSchedule schedule = sysModeScheduleMapper.selectSysModeScheduleById(scheduleId);
        if (schedule != null) {
            schedule.setRobots(sysModeScheduleMapper.selectRobotsByScheduleId(scheduleId));
        }
        return schedule;
    }

    /**
     * 查询排程列表
     *
     * @param sysModeSchedule 排程
     * @return 排程
     */
    @Override
    public List<SysModeSchedule> selectSysModeScheduleList(SysModeSchedule sysModeSchedule)
    {
        List<SysModeSchedule> list = sysModeScheduleMapper.selectSysModeScheduleList(sysModeSchedule);
        // 为每个排程加载关联的机器人
        for (SysModeSchedule schedule : list) {
            schedule.setRobots(sysModeScheduleMapper.selectRobotsByScheduleId(schedule.getScheduleId()));
        }
        return list;
    }

    /**
     * 新增排程
     *
     * @param sysModeSchedule 排程
     * @return 结果
     */
    @Override
    @Transactional
    public int insertSysModeSchedule(SysModeSchedule sysModeSchedule)
    {
        sysModeSchedule.setCreateTime(DateUtils.getNowDate());
        // 生成执行时间描述
        String executeTime = DateUtils.parseDateToStr("yyyy-MM-dd", sysModeSchedule.getStartDate())
                + " " + sysModeSchedule.getStartTime();
        sysModeSchedule.setExecuteTime(executeTime);

        int rows = sysModeScheduleMapper.insertSysModeSchedule(sysModeSchedule);
        // 插入机器人关联
        if (sysModeSchedule.getRobotIds() != null && sysModeSchedule.getRobotIds().length > 0) {
            sysModeScheduleMapper.insertScheduleRobots(sysModeSchedule.getScheduleId(), sysModeSchedule.getRobotIds());
        }
        return rows;
    }

    /**
     * 修改排程
     *
     * @param sysModeSchedule 排程
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSysModeSchedule(SysModeSchedule sysModeSchedule)
    {
        sysModeSchedule.setUpdateTime(DateUtils.getNowDate());
        // 更新执行时间描述
        String executeTime = DateUtils.parseDateToStr("yyyy-MM-dd", sysModeSchedule.getStartDate())
                + " " + sysModeSchedule.getStartTime();
        sysModeSchedule.setExecuteTime(executeTime);

        // 先删除旧的机器人关联
        sysModeScheduleMapper.deleteScheduleRobots(sysModeSchedule.getScheduleId());
        // 再插入新的机器人关联
        if (sysModeSchedule.getRobotIds() != null && sysModeSchedule.getRobotIds().length > 0) {
            sysModeScheduleMapper.insertScheduleRobots(sysModeSchedule.getScheduleId(), sysModeSchedule.getRobotIds());
        }
        return sysModeScheduleMapper.updateSysModeSchedule(sysModeSchedule);
    }

    /**
     * 批量删除排程
     *
     * @param scheduleIds 需要删除的排程ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysModeScheduleByIds(Long[] scheduleIds)
    {
        // 删除关联的机器人
        for (Long scheduleId : scheduleIds) {
            sysModeScheduleMapper.deleteScheduleRobots(scheduleId);
        }
        return sysModeScheduleMapper.deleteSysModeScheduleByIds(scheduleIds);
    }

    /**
     * 删除排程信息
     *
     * @param scheduleId 排程ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysModeScheduleById(Long scheduleId)
    {
        sysModeScheduleMapper.deleteScheduleRobots(scheduleId);
        return sysModeScheduleMapper.deleteSysModeScheduleById(scheduleId);
    }

    /**
     * 切换排程状态
     *
     * @param scheduleId 排程ID
     * @return 结果
     */
    @Override
    public int toggleScheduleStatus(Long scheduleId)
    {
        SysModeSchedule schedule = sysModeScheduleMapper.selectSysModeScheduleById(scheduleId);
        if (schedule != null) {
            String newStatus = "running".equals(schedule.getStatus()) ? "paused" : "running";
            return sysModeScheduleMapper.updateScheduleStatus(scheduleId, newStatus);
        }
        return 0;
    }
}