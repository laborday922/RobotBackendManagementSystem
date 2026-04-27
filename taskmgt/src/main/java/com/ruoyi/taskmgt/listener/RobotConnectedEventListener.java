package com.ruoyi.taskmgt.listener;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.event.RobotConnectedEvent;
import com.ruoyi.robots.event.RobotWarningEvent;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.impl.StepReuseService;
import com.ruoyi.taskmgt.service.impl.StepWebSocketService;
import com.ruoyi.taskmgt.service.impl.TaskLogReuseService;
import com.ruoyi.taskmgt.service.trigger.TaskTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class RobotConnectedEventListener {
    private final StepRepository stepRepository;
    private final TaskRepository taskRepository;
    private final TaskLogReuseService taskLogService;
    private final RedisCache redisUtil;
    private final StepWebSocketService stepWebSocketService;

    @EventListener
    public void handleRobotWarning(RobotConnectedEvent event) {
        log.info("接收到机器人在线状态改变事件：robotId={}, isConnected={}",
                event.getRobotId(), event.isConnected());
        if(!event.isConnected()){
            List<TaskStep>taskSteps = stepRepository.getSteps(TaskStep.EXECUTING,null,event.getRobotId());
            taskSteps.addAll(stepRepository.getSteps(TaskStep.WAITING,null,event.getRobotId()));
            taskSteps.addAll(stepRepository.getSteps(TaskStep.WAITING_CALLBACK,null,event.getRobotId()));
            if(!StringUtils.isEmpty(taskSteps)){
                List<String> redisKeys = new ArrayList<>();
                for(TaskStep step : taskSteps){
                    if(Objects.equals(step.getStatus(), TaskStep.EXECUTING)|| Objects.equals(step.getStatus(), TaskStep.WAITING)
                            || Objects.equals(step.getStatus(), TaskStep.WAITING_CALLBACK)){
                        step.setStatus(TaskStep.PAUSED);
                        taskLogService.record(
                                step.getTaskId(),
                                step.getId(),
                                TaskLogEventType.STEP_PAUSE,
                                " 步骤" + step.getStepName() + "已暂停",
                                "system",
                                null);
                        redisKeys.addAll(this.stepRepository.update(step));
                        Task task = taskRepository.findById(step.getTaskId()).orElse(null);
                        if(task!=null&&Objects.equals(task.getStatus(), Task.EXECUTING)){
                            task.setRiskLevel(2);
                            task.setUpdateBy("system");
                            redisKeys.addAll(taskRepository.update(task));
                            taskLogService.record(task.getId(), null, TaskLogEventType.TASK_PAUSE,
                                    "机器人离线，任务自动暂停", "system", null);
                        }
                        stepWebSocketService.sendStepChangeToRobot(step.getAssignedRobotId(),"pause",step,task);
                    }
                }
                this.redisUtil.deleteObject(redisKeys);
            }
        }
    }
}