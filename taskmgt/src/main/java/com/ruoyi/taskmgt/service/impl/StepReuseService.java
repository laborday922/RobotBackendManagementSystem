package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.ParamBinding;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.domain.bo.TaskStepDefinition;
import com.ruoyi.taskmgt.domain.bo.Template;
import com.ruoyi.taskmgt.utils.ExpressionEvaluator;
import com.ruoyi.taskmgt.utils.ParamValidator;
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
    private final ITAppApiService apiService;
    private final ExpressionEvaluator expressionEvaluator;
    private final ObjectMapper objectMapper;
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

    public List<String> cancelStepsByTaskId(Long taskId) {
        List<TaskStep> steps = this.stepRepository.findStepsByTaskId(taskId);
        List<String> redisKeys = new ArrayList<>();
        for(TaskStep step : steps){
            step.setStatus(TaskStep.NOTSTART);
            redisKeys.addAll(this.stepRepository.update(step));
        }
        return redisKeys;
    }

    public List<TaskStep> insertSteps(Template template, Map<String, Object> formDataMap, Map<String, Object> appParams) {
        List<TaskStepDefinition> stepDefs = template.getStepDefinitions();
        List<TaskStep> steps = new ArrayList<>();
        for (int i = 0; i < stepDefs.size(); i++) {
            TaskStepDefinition def = stepDefs.get(i);
            Long operationId = apiService.selectTAppApiById(def.getApiId()).getId();

            Map<String, Object> params = new HashMap<>();
            for (ParamBinding binding : def.getParams()) {
                Object value = null;
                switch (binding.getSource()) {
                    case "form_field":
                        value = formDataMap.get(binding.getValue());
                        break;
                    case "app_param":
                        value = appParams.get(binding.getValue());
                        break;
                    case "fixed":
                        value = binding.getValue();
                        break;
                    case "expression":
                        value = expressionEvaluator.evaluateExpression(binding.getValue(), Map.of("form_data", formDataMap, "app_param", appParams));
                        break;
                }
                params.put(binding.getName(), value);
            }
            String operationJson = null;
            try {
                operationJson = ParamValidator.objectMapper.writeValueAsString(params);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            TaskStep step = new TaskStep();
            step.setStepName(def.getName());
            step.setOrderNum(i + 1);
            step.setOperationId(operationId);
            step.setOperationJson(operationJson);
            step.setDescription(def.getDescription());
            steps.add(step);
        }
        return steps;
    }
}
