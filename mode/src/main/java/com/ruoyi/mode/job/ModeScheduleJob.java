package com.ruoyi.mode.job;

import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.enums.ScheduleStatusEnum;
import com.ruoyi.mode.service.ISysModeScheduleService;
import com.ruoyi.mode.service.ISysRobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 模式排程定时任务
 */
@Component
public class ModeScheduleJob {

    private static final Logger logger = LoggerFactory.getLogger(ModeScheduleJob.class);

    @Autowired
    private ISysModeScheduleService scheduleService;

    @Autowired
    private ISysRobotService robotService;

    /**
     * 每分钟执行一次，检查并执行到期的排程
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executePendingSchedules() {
        logger.debug("开始检查待执行的排程...");

        try {
            SysModeSchedule query = new SysModeSchedule();
            query.setStatus(ScheduleStatusEnum.RUNNING.getCode());
            List<SysModeSchedule> schedules = scheduleService.selectSysModeScheduleList(query);

            LocalDateTime now = LocalDateTime.now();

            for (SysModeSchedule schedule : schedules) {
                checkAndExecuteSchedule(schedule, now);
            }
        } catch (Exception e) {
            logger.error("执行排程检查失败", e);
        }
    }

    /**
     * 检查并执行单个排程
     */
    private void checkAndExecuteSchedule(SysModeSchedule schedule, LocalDateTime now) {
        try {
            LocalDateTime scheduleTime = getScheduleDateTime(schedule);

            if (scheduleTime != null && !now.isBefore(scheduleTime)) {
                // 检查是否需要执行
                if (shouldExecute(schedule, scheduleTime, now)) {
                    executeSchedule(schedule);
                }
            }
        } catch (Exception e) {
            logger.error("检查排程失败: scheduleId={}", schedule.getScheduleId(), e);
        }
    }

    /**
     * 获取排程的执行时间
     */
    private LocalDateTime getScheduleDateTime(SysModeSchedule schedule) {
        if (schedule.getStartDate() == null || schedule.getStartTime() == null) {
            return null;
        }

        LocalDate date = schedule.getStartDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        LocalTime time = LocalTime.parse(schedule.getStartTime());

        return LocalDateTime.of(date, time);
    }

    /**
     * 判断是否应该执行
     */
    private boolean shouldExecute(SysModeSchedule schedule, LocalDateTime scheduleTime, LocalDateTime now) {
        // 检查是否已执行过
        if (schedule.getLastExecuteTime() != null) {
            LocalDateTime lastExecute = schedule.getLastExecuteTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            // 同一分钟内不重复执行
            if (lastExecute.isAfter(now.minusMinutes(1))) {
                return false;
            }
        }

        // 检查重复类型
        switch (schedule.getRepeatType()) {
            case "once":
                return schedule.getLastExecuteTime() == null;
            case "daily":
                return true;
            case "weekly":
                return scheduleTime.getDayOfWeek().getValue() == now.getDayOfWeek().getValue();
            case "weekdays":
                return now.getDayOfWeek().getValue() >= 1 && now.getDayOfWeek().getValue() <= 5;
            case "monthly":
                return scheduleTime.getDayOfMonth() == now.getDayOfMonth();
            default:
                return true;
        }
    }

    /**
     * 执行排程
     */
    private void executeSchedule(SysModeSchedule schedule) {
        logger.info("开始执行排程: scheduleId={}, scheduleName={}",
                schedule.getScheduleId(), schedule.getScheduleName());

        try {
            List<SysRobot> robots = schedule.getRobots();
            if (robots != null && !robots.isEmpty()) {
                Long[] robotIds = robots.stream()
                        .map(SysRobot::getRobotId)
                        .toArray(Long[]::new);

                // 执行模式切换
                for (Long robotId : robotIds) {
                    robotService.updateRobotMode(robotId, schedule.getModeId());
                    logger.info("排程执行模式切换: robotId={}, modeId={}", robotId, schedule.getModeId());
                }
            }

            // 更新排程状态
            schedule.setLastExecuteTime(new java.util.Date());
            schedule.setLastExecuteStatus("success");
            scheduleService.updateSysModeSchedule(schedule);

            logger.info("排程执行成功: scheduleId={}", schedule.getScheduleId());

        } catch (Exception e) {
            logger.error("排程执行失败: scheduleId={}", schedule.getScheduleId(), e);
            schedule.setLastExecuteStatus("failed");
            scheduleService.updateSysModeSchedule(schedule);
        }
    }

    /**
     * 每天凌晨2点清理已完成和失败的排程
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredSchedules() {
        logger.info("开始清理过期排程...");

        try {
            SysModeSchedule query = new SysModeSchedule();
            query.setStatus(ScheduleStatusEnum.COMPLETED.getCode());
            List<SysModeSchedule> completedSchedules = scheduleService.selectSysModeScheduleList(query);

            query.setStatus(ScheduleStatusEnum.FAILED.getCode());
            List<SysModeSchedule> failedSchedules = scheduleService.selectSysModeScheduleList(query);

            // 清理30天前的已完成/失败排程
            java.util.Date thirtyDaysAgo = java.sql.Timestamp.valueOf(
                    LocalDateTime.now().minusDays(30));

            for (SysModeSchedule schedule : completedSchedules) {
                if (schedule.getCreateTime() != null &&
                        schedule.getCreateTime().before(thirtyDaysAgo)) {
                    scheduleService.deleteSysModeScheduleById(schedule.getScheduleId());
                    logger.info("清理过期排程: scheduleId={}", schedule.getScheduleId());
                }
            }

            for (SysModeSchedule schedule : failedSchedules) {
                if (schedule.getCreateTime() != null &&
                        schedule.getCreateTime().before(thirtyDaysAgo)) {
                    scheduleService.deleteSysModeScheduleById(schedule.getScheduleId());
                    logger.info("清理失败排程: scheduleId={}", schedule.getScheduleId());
                }
            }

            logger.info("过期排程清理完成");
        } catch (Exception e) {
            logger.error("清理过期排程失败", e);
        }
    }
}