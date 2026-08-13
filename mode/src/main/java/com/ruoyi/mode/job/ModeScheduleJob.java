package com.ruoyi.mode.job;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.mode.constants.HistoryConstants;
import com.ruoyi.mode.constants.ModeConstants;
import com.ruoyi.mode.domain.SysModeHistory;
import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.service.ISysModeHistoryService;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 模式排程定时执行Job
 * <p>
 * 每分钟巡检一次启用的排程，维护排程运行状态（pending/running），
 * 并在到达执行时间时对排程关联的机器人下发模式切换指令，同时写入历史日志。
 */
@Component
public class ModeScheduleJob {

    private static final Logger logger = LoggerFactory.getLogger(ModeScheduleJob.class);

    @Autowired
    private ISysModeScheduleService scheduleService;

    @Autowired
    private ISysRobotService robotService;

    @Autowired
    private ISysModeHistoryService historyService;

    /**
     * 每分钟执行一次排程巡检
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executeDueSchedules() {
        try {
            List<SysModeSchedule> schedules = scheduleService.selectEnabledSchedules();
            if (schedules == null || schedules.isEmpty()) {
                return;
            }

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);

            for (SysModeSchedule schedule : schedules) {
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
     * 处理单个排程：维护状态，必要时执行切换
     * <p>
     * 状态流转：
     * pending（待执行）--到达开始时间--> running（执行中，机器人处于目标模式）
     * running --持续时间结束--> 切回待机模式 --> completed（单次）/ pending（重复）
     */
    private void processSchedule(SysModeSchedule schedule, LocalDate today, LocalTime now) {
        String status = schedule.getStatus();
        // 终态（已完成/失败）与停用不再处理
        if ("completed".equals(status) || "failed".equals(status) || "paused".equals(status)) {
            return;
        }

        boolean inPeriod = matchesToday(schedule, today) && isInPeriod(schedule, now);

        if (inPeriod) {
            // 在排程期间内：只在待执行时下发一次目标模式，进入执行中
            if ("pending".equals(status)) {
                executeSchedule(schedule);
            }
            // 已是 running 则保持，不重复下发
        } else {
            // 不在期间内：若之前在运行，说明持续时间已到，切回待机模式
            if ("running".equals(status)) {
                finishSchedule(schedule);
            }
        }
    }

    /**
     * 执行排程：对关联机器人下发目标模式，写历史日志，状态置为进行中
     */
    private void executeSchedule(SysModeSchedule schedule) {
        logger.info("排程 [{}] 触发执行: modeId={}, startTime={}",
                schedule.getScheduleName(), schedule.getModeId(), schedule.getStartTime());

        List<Robot> robots = schedule.getRobots();
        if (robots == null || robots.isEmpty()) {
            logger.warn("排程 {} 没有关联机器人，标记为失败", schedule.getScheduleName());
            scheduleService.updateScheduleExecutionResult(schedule.getScheduleId(), "failed", new Date());
            scheduleService.updateScheduleStatus(schedule.getScheduleId(), "failed");
            return;
        }

        boolean allSuccess = true;
        for (Robot robot : robots) {
            boolean ok = false;
            try {
                ok = robotService.updateRobotMode(robot.getId(), schedule.getModeId()) > 0;
            } catch (Exception e) {
                logger.error("  排程机器人 {} 模式切换异常", robot.getName(), e);
                ok = false;
            }
            writeHistory(schedule, robot, schedule.getModeId(), "自动切换目标模式", ok);
            if (!ok) {
                allSuccess = false;
            }
        }

        // 记录本次下发结果
        String lastExecuteStatus = allSuccess ? "success" : "failed";
        scheduleService.updateScheduleExecutionResult(
                schedule.getScheduleId(), lastExecuteStatus, new Date());

        // 进入执行中状态（持续时间由 isInPeriod 控制，结束后统一切回待机）
        scheduleService.updateScheduleStatus(schedule.getScheduleId(), "running");
        logger.info("排程 [{}] 已下发目标模式，状态置为进行中(running)", schedule.getScheduleName());
    }

    /**
     * 结束排程：持续时间结束，切回待机模式，写历史日志，并推进终态
     */
    private void finishSchedule(SysModeSchedule schedule) {
        logger.info("排程 [{}] 持续时间结束，切回待机模式", schedule.getScheduleName());

        List<Robot> robots = schedule.getRobots();
        if (robots != null && !robots.isEmpty()) {
            for (Robot robot : robots) {
                boolean ok = false;
                try {
                    ok = robotService.updateRobotMode(robot.getId(), ModeConstants.DEFAULT_MODE_ID) > 0;
                } catch (Exception e) {
                    logger.error("  排程机器人 {} 切回待机模式异常", robot.getName(), e);
                }
                writeHistory(schedule, robot, ModeConstants.DEFAULT_MODE_ID, "切回待机模式", ok);
            }
        }

        // 单次 -> 已完成；重复 -> 待执行（等待下一次触发）
        if ("once".equals(schedule.getRepeatType())) {
            scheduleService.updateScheduleStatus(schedule.getScheduleId(), "completed");
            logger.info("单次排程 [{}] 执行完成", schedule.getScheduleName());
        } else {
            scheduleService.updateScheduleStatus(schedule.getScheduleId(), "pending");
            logger.info("重复排程 [{}] 期间结束，状态回到待执行(pending)", schedule.getScheduleName());
        }
    }

    /**
     * 写入模式切换历史日志
     */
    private void writeHistory(SysModeSchedule schedule, Robot robot, Long modeId, String action, boolean success) {
        try {
            SysModeHistory history = new SysModeHistory();
            history.setOperationType(HistoryConstants.OPERATION_TYPE_SCHEDULE);
            history.setRobotId(robot.getId());
            history.setModeId(modeId);
            history.setOperator("schedule");
            history.setStatus(success ? HistoryConstants.STATUS_SUCCESS : HistoryConstants.STATUS_DANGER);
            history.setContent(String.format("排程 [%s] %s%s",
                    schedule.getScheduleName(), action, success ? "成功" : "失败"));
            history.setOperationTime(new Date());
            history.setTenantId(TenantContext.get());
            historyService.insertSysModeHistory(history);
        } catch (Exception e) {
            logger.error("写入排程历史日志失败: scheduleId={}, robotId={}",
                    schedule.getScheduleId(), robot.getId(), e);
        }
    }

    /**
     * 检查今天是否符合排程的重复规则
     */
    private boolean matchesToday(SysModeSchedule schedule, LocalDate today) {
        String repeatType = schedule.getRepeatType();
        if (repeatType == null) return false;

        switch (repeatType) {
            case "once":
                if (schedule.getStartDate() != null) {
                    return today.equals(toLocalDate(schedule.getStartDate()));
                }
                return false;

            case "daily":
                return true;

            case "weekdays":
                int dayOfWeek = today.getDayOfWeek().getValue();
                return dayOfWeek >= 1 && dayOfWeek <= 5;

            case "weekly":
                return isInRuleDays(schedule.getRepeatRule(), "weekly", today.getDayOfWeek().getValue());

            case "monthly":
                return isInRuleDays(schedule.getRepeatRule(), "monthly", today.getDayOfMonth());

            default:
                return false;
        }
    }

    /**
     * 检查当前时间是否处于排程的 [startTime, startTime+duration) 期间内
     */
    private boolean isInPeriod(SysModeSchedule schedule, LocalTime now) {
        LocalTime start = parseStartTime(schedule.getStartTime());
        if (start == null) return false;

        long durationMinutes = 60; // 默认1小时
        if (schedule.getDuration() != null) {
            durationMinutes = (long) (schedule.getDuration().doubleValue() * 60);
        }
        LocalTime end = start.plusMinutes(durationMinutes);
        // 不处理跨天场景，跨天视为不在期间内
        if (!end.isAfter(start)) {
            return false;
        }
        return !now.isBefore(start) && now.isBefore(end);
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
            String t = startTime.trim();
            if (t.length() >= 5) {
                int hour = Integer.parseInt(t.substring(0, 2));
                int minute = Integer.parseInt(t.substring(3, 5));
                return LocalTime.of(hour, minute);
            }
            return null;
        } catch (Exception e) {
            logger.warn("解析开始时间失败: {}", startTime, e);
            return null;
        }
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
