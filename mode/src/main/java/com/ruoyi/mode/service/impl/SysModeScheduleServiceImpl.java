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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 获取当前租户ID
     */
    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    /**
     * 查询排程
     *
     * @param scheduleId 排程ID
     * @return 排程
     */
    @Override
    public SysModeSchedule selectSysModeScheduleById(Long scheduleId)
    {
        SysModeSchedule query = new SysModeSchedule();
        query.setScheduleId(scheduleId);
        query.setTenantId(getCurrentTenantId());

        SysModeSchedule schedule = sysModeScheduleMapper.selectSysModeScheduleById(query);
        if (schedule != null) {
            schedule.setRobots(sysModeScheduleMapper.selectRobotsByScheduleId(scheduleId, getCurrentTenantId()));
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
        if (sysModeSchedule == null) {
            sysModeSchedule = new SysModeSchedule();
        }
        sysModeSchedule.setTenantId(getCurrentTenantId());

        List<SysModeSchedule> list = sysModeScheduleMapper.selectSysModeScheduleList(sysModeSchedule);
        Long tenantId = getCurrentTenantId();
        for (SysModeSchedule schedule : list) {
            schedule.setRobots(sysModeScheduleMapper.selectRobotsByScheduleId(schedule.getScheduleId(), tenantId));
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
        sysModeSchedule.setTenantId(getCurrentTenantId());
        sysModeSchedule.setCreateTime(DateUtils.getNowDate());

        String executeTime = DateUtils.parseDateToStr("yyyy-MM-dd", sysModeSchedule.getStartDate())
                + " " + sysModeSchedule.getStartTime();
        sysModeSchedule.setExecuteTime(executeTime);

        int rows = sysModeScheduleMapper.insertSysModeSchedule(sysModeSchedule);
        if (sysModeSchedule.getRobotIds() != null && sysModeSchedule.getRobotIds().length > 0) {
            sysModeScheduleMapper.insertScheduleRobots(
                    sysModeSchedule.getScheduleId(),
                    sysModeSchedule.getRobotIds(),
                    getCurrentTenantId()
            );
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

        String executeTime = DateUtils.parseDateToStr("yyyy-MM-dd", sysModeSchedule.getStartDate())
                + " " + sysModeSchedule.getStartTime();
        sysModeSchedule.setExecuteTime(executeTime);

        // 先删除旧的机器人关联
        sysModeScheduleMapper.deleteScheduleRobots(sysModeSchedule.getScheduleId());
        // 再插入新的机器人关联
        if (sysModeSchedule.getRobotIds() != null && sysModeSchedule.getRobotIds().length > 0) {
            sysModeScheduleMapper.insertScheduleRobots(
                    sysModeSchedule.getScheduleId(),
                    sysModeSchedule.getRobotIds(),
                    getCurrentTenantId()
            );
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
        SysModeSchedule query = new SysModeSchedule();
        query.setScheduleId(scheduleId);
        query.setTenantId(getCurrentTenantId());
        SysModeSchedule schedule = sysModeScheduleMapper.selectSysModeScheduleById(query);
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