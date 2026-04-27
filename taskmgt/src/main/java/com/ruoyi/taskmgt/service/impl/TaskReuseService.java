package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TaskReuseService {
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogService;
    private final TaskRepository taskRepository;
    private final RedisCache redisUtil;
    public void completeTask(Task task) {
        if (!task.allowTransitStatus(Task.FINISHED)) {
            log.warn("任务 {} 无法转为已完成", task.getId());
            return;
        }

        Date nextTime = calculateNextScheduledTime(task);
        boolean isPeriodic = (task.getTaskType() != null && task.getTaskType() == 1) || StringUtils.isNotBlank(task.getCronExpression());

        if (isPeriodic && nextTime != null) {
            // 定时任务，重置为未开始状态
            task.setStatus(Task.NOTSTART);
            task.setScheduledTime(nextTime);
            task.setPendingOrder(null);
            task.setGlobalPendingOrder(null);
            task.setRiskLevel(0);
            // 重置所有步骤状态
            List<TaskStep> steps = stepRepository.findStepsByTaskId(task.getId());
            for (TaskStep step : steps) {
                step.setStatus(TaskStep.NOTSTART);
                step.setStartTime(null);
                step.setEndTime(null);
                if(task.getIsGroupTask()==1)step.setAssignedRobotId(null);
                stepRepository.update(step);
            }
            taskLogService.record(task.getId(), null, TaskLogEventType.TASK_COMPLETE,
                    "周期性任务完成，下次执行时间：" + nextTime, "system", null);
        } else {
            // 一次性任务，直接完成
            task.setStatus(Task.FINISHED);
            List<TaskStep> steps = stepRepository.findStepsByTaskId(task.getId());
            Integer duration = 0 ;
            for (TaskStep step : steps) {
                Date startTime = step.getStartTime();
                Date endTime = step.getEndTime();
                if (startTime != null && endTime != null) {
                    long diffMillis = endTime.getTime() - startTime.getTime();
                    int seconds = (int) (diffMillis / 1000);
                    duration += seconds;
                }
            }
            task.setDuration(duration);
            taskLogService.record(task.getId(), null, TaskLogEventType.TASK_COMPLETE,
                    "任务执行完成", "system", null);
        }

        task.setUpdateBy("system");
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        log.info("任务 {} 已完成处理，最终状态: {}", task.getId(), task.getStatus());
    }

    private Date calculateNextScheduledTime(Task task) {
        String cron = task.getCronExpression();
        if (cron == null || cron.trim().isEmpty()) {
            return null;
        }
        try {
            CronExpression cronExp = CronExpression.parse(cron);
            Date baseTime = task.getScheduledTime() != null ? task.getScheduledTime() : new Date();
            ZonedDateTime next = cronExp.next(baseTime.toInstant().atZone(ZoneId.systemDefault()));
            return next != null ? Date.from(next.toInstant()) : null;
        } catch (Exception e) {
            log.error("解析 cron 表达式失败: {}", cron, e);
            return null;
        }
    }
}
