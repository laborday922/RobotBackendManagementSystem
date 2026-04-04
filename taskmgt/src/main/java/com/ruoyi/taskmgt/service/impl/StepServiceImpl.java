package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.vo.TaskStepVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StepServiceImpl implements IStepService {
    private final TaskRepository taskRepository;
    private final MessageSourceAccessor messageSourceAccessor;
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogReuseService;
    private final RedisCache redisUtil;

    @Override
    public List<TaskStepVo> createSteps(Long taskId, List<TaskStep> steps) {
        Long tenantId = TenantContext.get();
        if(isAdmin(tenantId))tenantId=null;
        if(StringUtils.isEmpty(steps))return List.of();
        //Assert.notEmpty(steps, "steps cannot be empty");
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        Long finalTenantId = tenantId;
        List<TaskStep> savedSteps = steps.stream()
                .map(step -> {
                    step.setTenantId(finalTenantId);
                    step.setTaskId(taskId);
                    step.setStatus(TaskStep.NOTSTART);
                    return this.stepRepository.insert(step);
                })
                .toList();

        return savedSteps.stream()
                .map(step -> {
                    TaskStepVo vo = CloneFactory.copy(new TaskStepVo(), step);
                    vo.setTaskName(task.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateSteps(Long taskId, List<TaskStep> steps) {
        Long tenantId = TenantContext.get();
        if(isAdmin(tenantId))tenantId=null;
        if (StringUtils.isEmpty(steps))return;
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });

        List<String> redisKeys = new ArrayList<>();
        for (TaskStep step : steps) {
            step.setTenantId(tenantId);
            if(step.getAssignedRobotId()!=null&&this.stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(step.getAssignedRobotId(),null,List.of(TaskStep.EXECUTING,TaskStep.WAITING,TaskStep.WAITING_CALLBACK))!=0)
            {
                task.setStatus(Task.PENDING);
                task.setUpdateBy(SecurityUtils.getUsername());
                task.setUpdateTime(DateUtils.getNowDate());
                redisKeys.addAll(this.taskRepository.update(task));
            }
            TaskStep orginStep = this.stepRepository.findStepByTaskIdAndOrder(taskId,step.getOrderNum());
            if(StringUtils.isNotNull(orginStep)){
                step.setId(orginStep.getId());
                redisKeys.addAll(this.stepRepository.update(step));
            }
            else this.stepRepository.insert(step);
        }
        this.redisUtil.deleteObject(redisKeys);

    }

    @Override
    public List<TaskStepVo> retrieveSteps(Long taskId) {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        List<TaskStep> taskSteps = this.stepRepository.findStepsByTaskId(taskId);
        return taskSteps.stream()
                .map(step -> {
                    TaskStepVo vo = CloneFactory.copy(new TaskStepVo(), step);
                    vo.setTaskName(task.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void completeStep(Long stepId) {
        TaskStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> {
                    String[] args = {messageSourceAccessor.getMessage("Step.name", LocaleContextHolder.getLocale()), stepId.toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
                            messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });

        if (!Objects.equals(step.getStatus(), TaskStep.EXECUTING)) {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Step.name", LocaleContextHolder.getLocale()), step.getId().toString(), step.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW, args, this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }

        // 标记当前步骤完成
        step.setStatus(TaskStep.FINISHED);
        step.setEndTime(new Date());
        List<String> redisKeys = stepRepository.update(step);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogReuseService.record(step.getTaskId(), stepId, TaskLogEventType.STEP_COMPLETE,
                "步骤 " + step.getStepName() + " 完成" + "开始时间:" + step.getStartTime()+
                        "结束时间:" + step.getEndTime(), "system", null);

        // 查找下一个步骤
        List<TaskStep> steps = stepRepository.findStepsByTaskId(step.getTaskId());
        TaskStep nextStep = steps.stream()
                .filter(s -> s.getOrderNum() > step.getOrderNum())
                .min(Comparator.comparing(TaskStep::getOrderNum))
                .orElse(null);

        if (nextStep != null && Objects.equals(nextStep.getStatus(), TaskStep.NOTSTART)) {
            startStep(nextStep);
        } else {
            // 所有步骤完成，任务结束
            Task task = taskRepository.findById(step.getTaskId())
                    .orElseThrow(() -> {
                        String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), step.getTaskId().toString()};
                        return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                    });
            Date nextTime = calculateNextScheduledTime(task);
            if(task.allowTransitStatus(Task.FINISHED))task.setStatus(Task.FINISHED);
            if(task.getTaskType()!=1||nextTime!=null) {
                task.setScheduledTime(nextTime);
                if(task.allowTransitStatus(Task.NOTSTART))task.setStatus(Task.NOTSTART);
                for (TaskStep s : steps) {
                    if(step.allowTransitStatus(TaskStep.NOTSTART))s.setStatus(TaskStep.NOTSTART);
                    s.setStartTime(null);
                    s.setEndTime(null);
                    List<String> stepRedisKeys = stepRepository.update(s);
                    if (stepRedisKeys != null && !stepRedisKeys.isEmpty()) {
                        redisUtil.deleteObject(stepRedisKeys);
                    }
                }
            }
            task.setUpdateBy("system");
            List<String> taskRedisKeys = taskRepository.update(task);
            if (taskRedisKeys != null && !taskRedisKeys.isEmpty()) {
                redisUtil.deleteObject(taskRedisKeys);
            }
            taskLogReuseService.record(task.getId(), null, TaskLogEventType.TASK_COMPLETE,
                    "任务所有步骤完成，下一次执行时间：" + (nextTime != null ? nextTime : "无"), "system", null);
        }
    }

    private void startStep(TaskStep step) {
        step.setStatus(TaskStep.EXECUTING);
        step.setStartTime(new Date());
        step.setUpdateBy("system");
        List<String> redisKeys = stepRepository.update(step);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogReuseService.record(step.getTaskId(), step.getId(), TaskLogEventType.STEP_START,
                "步骤 " + step.getStepName() + " 开始执行", "system", null);
    }

    private Date calculateNextScheduledTime(Task task) {
        String cron = task.getCronExpression();
        if (cron == null || cron.trim().isEmpty()) {
            return null;
        }
        try {
            CronExpression cronExp = CronExpression.parse(cron);
            Date baseTime = task.getScheduledTime();
            if (baseTime == null) {
                baseTime = new Date();
            }
            ZonedDateTime baseZdt = baseTime.toInstant().atZone(ZoneId.systemDefault());
            ZonedDateTime nextZdt = cronExp.next(baseZdt);
            if (nextZdt != null) {
                return Date.from(nextZdt.toInstant());
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Invalid cron expression for task {}: {}", task.getId(), cron);
            return null;
        }
    }
}
