package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StepReuseService {
    private final StepRepository stepRepository ;
    private final TaskLogReuseService taskLogService;
    public List<String> pauseStepsByTaskId(Long taskId) {
        List<TaskStep> steps = this.stepRepository.findStepsByTaskId(taskId);
        List<String> redisKeys = new ArrayList<>();
        for(TaskStep step : steps){
            if(Objects.equals(step.getStatus(), TaskStep.EXECUTING)){
                step.setStatus(TaskStep.PAUSED);
                taskLogService.record(
                        taskId,
                        step.getId(),
                        TaskLogEventType.STEP_PAUSE,
                        " 步骤" + step.getStepName() + "已暂停",
                        "system"
                );
                redisKeys.addAll(this.stepRepository.update(step));
            }
        }
        return redisKeys;
    }

    public List<String> continueStepsByTaskId(Long taskId) {
        List<TaskStep> steps = this.stepRepository.findStepsByTaskId(taskId);
        List<String> redisKeys = new ArrayList<>();
        for(TaskStep step : steps){
            if(Objects.equals(step.getStatus(), TaskStep.PAUSED)){
                step.setStatus(TaskStep.EXECUTING);
                this.taskLogService.record(
                        taskId,
                        step.getId(),
                        TaskLogEventType.STEP_RESUME,
                        " 步骤" + step.getStepName() + "已继续",
                        "system"
                );
                redisKeys.addAll(this.stepRepository.update(step));
            }
        }
        return redisKeys;
    }

    public List<String> terminatedStepsByTaskId(Long taskId) {
        List<TaskStep> steps = this.stepRepository.findStepsByTaskId(taskId);
        List<String> redisKeys = new ArrayList<>();
        for(TaskStep step : steps){
            step.setStatus(TaskStep.TERMINATED);
            this.taskLogService.record(
                    taskId,
                    step.getId(),
                    TaskLogEventType.STEP_TERMINATE,
                    " 步骤" + step.getStepName() + "已终止",
                    "system"
            );
            redisKeys.addAll(this.stepRepository.update(step));
        }
        return redisKeys;
    }
}
