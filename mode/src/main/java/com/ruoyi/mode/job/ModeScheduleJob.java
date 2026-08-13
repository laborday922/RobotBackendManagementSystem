package com.ruoyi.mode.job;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.mode.constants.HistoryConstants;
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
import java.time.format.DateTimeFormatter;
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
     */
    private void processSchedule(SysModeSchedule schedule, LocalDate today, LocalTime now) {
        // 终态（单次已完成/执行失败）不再处理
        if ("completed".equals(schedule.getStatus()) || "failed".equals(schedule.getStatus())) {
            return;
        }

        boolean todayMatches = matchesToday(schedule, today);
        boolean inPeriod = todayMatches && isInPeriod(schedule, now);

        if (inPeriod) {
            // 在排程期间内
            if (!alreadyExecutedToday(schedule, today)) {
                // 今天还未执行，触发执行
                executeSchedule(schedule);
            } else if (!"running".equals(schedule.getStatus())) {
                // 已执行但状态不是进行中（例如期间内状态被改动），纠正为进行中
                scheduleService.updateScheduleStatus(schedule.getScheduleId(), "running");
            }
        } else {
            // 不在期间内：如果之前是进行中，说明期间结束，回到待执行
            if ("running".equals(schedule.getStatus())) {
                scheduleService.updateScheduleStatus(schedule.getScheduleId(), "pending");
                logger.info("排程 [{}] 期间结束，状态由 running 恢复为 pending", schedule.getScheduleName());
            }
        }
    }

    /**
     * 执行排程：对关联机器人下发模式切换，写历史日志
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
            // 写历史日志
            writeHistory(schedule, robot, ok);
            if (!ok) {
                allSuccess = false;
            }
        }

        // 记录执行结果
        String lastExecuteStatus = allSuccess ? "success" : "failed";
        scheduleService.updateScheduleExecutionResult(
                schedule.getScheduleId(), lastExecuteStatus, new Date());

        // 更新排程状态：单次 -> 已完成，重复 -> 进行中
        if ("once".equals(schedule.getRepeatType())) {
            scheduleService.updateScheduleStatus(schedule.getScheduleId(), "completed");
            logger.info("单次排程 [{}] 执行完成", schedule.getScheduleName());
        } else {
            scheduleService.updateScheduleStatus(schedule.getScheduleId(), "running");
            logger.info("重复排程 [{}] 执行完成，状态置为进行中", schedule.getScheduleName());
        }
    }

    /**
     * 写入模式切换历史日志
     */
    private void writeHistory(SysModeSchedule schedule, Robot robot, boolean success) {
        try {
            SysModeHistory history = new SysModeHistory();
            history.setOperationType(HistoryConstants.OPERATION_TYPE_SCHEDULE);
            history.setRobotId(robot.getId());
            history.setModeId(schedule.getModeId());
            history.setOperator("schedule");
            history.setStatus(success ? HistoryConstants.STATUS_SUCCESS : HistoryConstants.STATUS_DANGER);
            history.setContent(String.format("排程 [%s] 自动切换模式%s",
                    schedule.getScheduleName(), success ? "成功" : "失败"));
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
        return today.equals(toLocalDate(schedule.getLastExecuteTime()));
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
