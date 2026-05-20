package com.ruoyi.mode.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.mapper.SysModeScheduleMapper;
import com.ruoyi.mode.service.ISysModeScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

/**
 * 模式排程Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysModeScheduleServiceImpl implements ISysModeScheduleService
{
    private static final Logger logger = LoggerFactory.getLogger(SysModeScheduleServiceImpl.class);

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
            Long tenantId = TenantContext.get();
            schedule.setRobots(sysModeScheduleMapper.selectRobotsByScheduleId(scheduleId, isAdmin(tenantId) ? null : tenantId));
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
        // 设置租户过滤
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            sysModeSchedule.setTenantId(tenantId);

        List<SysModeSchedule> list = sysModeScheduleMapper.selectSysModeScheduleList(sysModeSchedule);
        // 为每个排程加载关联的机器人
        for (SysModeSchedule schedule : list) {
            schedule.setRobots(sysModeScheduleMapper.selectRobotsByScheduleId(schedule.getScheduleId(), isAdmin(tenantId) ? null : tenantId));
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
        // 设置租户ID
        sysModeSchedule.setTenantId(TenantContext.get());
        sysModeSchedule.setCreateTime(DateUtils.getNowDate());
        // 生成执行时间描述
        if (sysModeSchedule.getStartDate() != null && sysModeSchedule.getStartTime() != null) {
            String executeTime = DateUtils.parseDateToStr("yyyy-MM-dd", sysModeSchedule.getStartDate())
                    + " " + sysModeSchedule.getStartTime();
            sysModeSchedule.setExecuteTime(executeTime);
        }

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
     * 关键修改：只有当 robotIds 不为 null 时才更新机器人关联
     * 如果 robotIds 为 null，表示不修改机器人关联（保持原样）
     *
     * @param sysModeSchedule 排程
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSysModeSchedule(SysModeSchedule sysModeSchedule)
    {
        sysModeSchedule.setUpdateTime(DateUtils.getNowDate());

        // 更新执行时间描述（只有当 startDate 和 startTime 都存在时才更新）
        if (sysModeSchedule.getStartDate() != null && sysModeSchedule.getStartTime() != null) {
            String executeTime = DateUtils.parseDateToStr("yyyy-MM-dd", sysModeSchedule.getStartDate())
                    + " " + sysModeSchedule.getStartTime();
            sysModeSchedule.setExecuteTime(executeTime);
        }

        // 关键修改：只有当 robotIds 不为 null 时才更新机器人关联
        // 如果 robotIds 为 null，表示不修改机器人关联（保持原样）
        // 这样可以避免定时任务更新 lastExecuteTime 时误删机器人关联
        if (sysModeSchedule.getRobotIds() != null) {
            // 先删除旧的机器人关联
            sysModeScheduleMapper.deleteScheduleRobots(sysModeSchedule.getScheduleId());
            // 再插入新的机器人关联（只有长度大于0时才插入）
            if (sysModeSchedule.getRobotIds().length > 0) {
                sysModeScheduleMapper.insertScheduleRobots(sysModeSchedule.getScheduleId(), sysModeSchedule.getRobotIds());
            }
        }
        // 如果 robotIds 为 null，不执行任何删除/插入操作，保持原有机器人关联

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

    /**
     * 获取日历数据
     *
     * @param year 年份
     * @param month 月份（可选）
     * @return 日历数据
     */
    @Override
    public Map<String, Object> getCalendarData(Integer year, Integer month)
    {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("year", year);
            result.put("success", true);

            if (month != null) {
                // 查询指定月份的数据
                String beginTime = year + "-" + String.format("%02d", month) + "-01 00:00:00";
                String endTime = year + "-" + String.format("%02d", month) + "-31 23:59:59";

                // 获取该月份有排程的日期
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("year", year);
                monthData.put("month", month);
                monthData.put("beginTime", beginTime);
                monthData.put("endTime", endTime);

                result.put("monthData", monthData);
                result.put("message", "获取月份数据成功");
            } else {
                // 返回全年概览
                result.put("message", "获取年度数据成功");
            }
        } catch (Exception e) {
            logger.error("获取日历数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}