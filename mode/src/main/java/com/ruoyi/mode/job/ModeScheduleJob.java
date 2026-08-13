package com.ruoyi.mode.job;

import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.service.ISysModeScheduleService;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.robots.domain.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 模式排程定时执行Job
 * <p>
 * 每分钟巡检一次，检查是否有排程到了执行时间，
 * 如果是则对排程关联的机器人下发模式切换指令。
 */
@Component
public class ModeScheduleJob {

    private static final Logger logger = LoggerFactory.getLogger(ModeScheduleJob.class);

    @Autowired
    private ISysModeScheduleService scheduleService;

    @Autowired
    private ISysRobotService robotService;

    /**
     * 每分钟执行一次排程巡检
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executeDueSchedules() {
        logger.debug("开始巡检排程...");

        try {
            List<SysModeSchedule> runningSchedules = scheduleService.selectRunningSchedules();
            if (runningSchedules == null || runningSchedules.isEmpty()) {
                return;
            }

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);

            for (SysModeSchedule schedule : runningSchedules) {
                try {
                    processSchedule(schedule, today, now);
                } catch (Exception e) {
                    logger.error("处理排程失败: scheduleId={}, scheduleName={}",
                            schedule.getScheduleId(), schedule.getScheduleName(), e);
                }
            }
        } catch (Exception e) {
            logger.error("巡检排程异常", e);
        }
    }

    /**
     * 处理单个排程
     */
    private void processSchedule(SysModeSchedule schedule, LocalDate today, LocalTime now) {
        // 1. 检查今天是否应执行
        if (!matchesToday(schedule, today)) {
            return;
        }

        // 2. 解析排程的开始时间 (HH:mm)
        LocalTime startTime = parseStartTime(schedule.getStartTime());
        if (startTime == null) {
            return;
        }

        // 3. 检查是否到了执行时间（当前分钟 == 开始分钟）
        if (!now.equals(startTime)) {
            return;
        }

        // 4. 检查今天是否已执行过（防止重复）
        if (alreadyExecutedToday(schedule, today)) {
            logger.debug("排程 {} 今天已执行过，跳过", schedule.getScheduleName());
            return;
        }

        // 5. 执行模式切换
        logger.info("排程 [{}] 触发执行: modeId={}, startTime={}, robots={}",
                schedule.getScheduleName(), schedule.getModeId(),
                schedule.getStartTime(),
                schedule.getRobots() != null ? schedule.getRobots().size() : 0);

        List<Robot> robots = schedule.getRobots();
        if (robots == null || robots.isEmpty()) {
            logger.warn("排程 {} 没有关联机器人，跳过", schedule.getScheduleName());
            scheduleService.updateScheduleExecutionStatus(
                    schedule.getScheduleId(), "failed", new Date(), "没有关联机器人");
            return;
        }

        boolean allSuccess = true;
        for (Robot robot : robots) {
            try {
                int result = robotService.updateRobotMode(robot.getId(), schedule.getModeId());
                if (result > 0) {
                    logger.info("  排程机器人 {} 模式切换成功", robot.getName());
                } else {
                    logger.warn("  排程机器人 {} 模式切换失败", robot.getName());
                    allSuccess = false;
                }
            } catch (Exception e) {
                logger.error("  排程机器人 {} 模式切换异常", robot.getName(), e);
                allSuccess = false;
            }
        }

        // 6. 更新执行状态
        String status = allSuccess ? "success" : "failed";
        scheduleService.updateScheduleExecutionStatus(
                schedule.getScheduleId(), status, new Date(),
                allSuccess ? null : "部分机器人切换失败");

        // 7. 单次排程执行后标记为已完成
        if ("once".equals(schedule.getRepeatType())) {
            scheduleService.completeSchedule(schedule.getScheduleId());
            logger.info("单次排程 {} 已完成", schedule.getScheduleName());
        }

        logger.info("排程 [{}] 执行完成: status={}", schedule.getScheduleName(), status);
    }

    /**
     * 检查今天是否符合排程的重复规则
     */
    private boolean matchesToday(SysModeSchedule schedule, LocalDate today) {
        String repeatType = schedule.getRepeatType();
        if (repeatType == null) return false;

        switch (repeatType) {
            case "once":
                // 单次：检查 startDate 是否等于今天
                if (schedule.getStartDate() != null) {
                    LocalDate startDate = toLocalDate(schedule.getStartDate());
                    return today.equals(startDate);
                }
                return false;

            case "daily":
                return true;

            case "weekdays":
                // 工作日：周一到周五
                int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
                return dayOfWeek >= 1 && dayOfWeek <= 5;

            case "weekly": {
                // 每周：检查今天是周几，是否在 repeatRule 的 days 中
                int cnWeekDay = today.getDayOfWeek().getValue(); // 1=Mon=1, 7=Sun=7
                return isInRuleDays(schedule.getRepeatRule(), "weekly", cnWeekDay);
            }

            case "monthly": {
                // 每月：检查今天几号，是否在 repeatRule 的 days 中
                int dayOfMonth = today.getDayOfMonth();
                return isInRuleDays(schedule.getRepeatRule(), "monthly", dayOfMonth);
            }

            default:
                return false;
        }
    }

    /**
     * 解析 repeatRule JSON，检查目标值是否在 days 数组中
     */
    private boolean isInRuleDays(String repeatRule, String expectedType, int targetDay) {
        if (repeatRule == null || repeatRule.isEmpty()) return false;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> rule = mapper.readValue(repeatRule, java.util.Map.class);
            if (expectedType.equals(rule.get("type"))) {
                @SuppressWarnings("unchecked")
                java.util.List<Integer> days = (java.util.List<Integer>) rule.get("days");
                if (days != null && days.contains(targetDay)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("解析 repeatRule 失败: {}", repeatRule, e);
        }
        return false;
    }

    /**
     * 解析开始时间 HH:mm
     */
    private LocalTime parseStartTime(String startTime) {
        if (startTime == null || startTime.isEmpty()) return null;
        try {
            return LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            logger.warn("解析开始时间失败: {}", startTime, e);
            return null;
        }
    }

    /**
     * 检查排程今天是否已执行过
     */
    private boolean alreadyExecutedToday(SysModeSchedule schedule, LocalDate today) {
        if (schedule.getLastExecuteTime() == null) return false;
        LocalDate lastExecuteDate = toLocalDate(schedule.getLastExecuteTime());
        return today.equals(lastExecuteDate);
    }

    /**
     * Date 转 LocalDate
     */
    private LocalDate toLocalDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return LocalDate.of(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }
}
