package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.*;
import com.ruoyi.taskmgt.service.ITaskParamsService;
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
    private final ITaskParamsService taskParamsService;
    private final ExpressionEvaluator expressionEvaluator;
    public List<String> pauseStepsByTaskId(Long taskId) {
        List<TaskStep> steps = this.stepRepository.findStepsByTaskId(taskId);
        List<String> redisKeys = new ArrayList<>();
        for(TaskStep step : steps){
            if(Objects.equals(step.getStatus(), TaskStep.EXECUTING)|| Objects.equals(step.getStatus(), TaskStep.WAITING)
                    || Objects.equals(step.getStatus(), TaskStep.WAITING_CALLBACK)){
                step.setStatus(TaskStep.PAUSED);
                taskLogService.record(
                        taskId,
                        step.getId(),
                        TaskLogEventType.STEP_PAUSE,
                        " 步骤" + step.getStepName() + "已暂停",
                        "system",
                        null);
                redisKeys.addAll(this.stepRepository.update(step));
            }
        }
        return redisKeys;
    }

    public List<String> continueStepsByTaskId(Long taskId) {
        List<TaskStep> steps = this.stepRepository.findStepsByTaskId(taskId);
        List<String> redisKeys = new ArrayList<>();
        for (TaskStep step : steps) {
            if (Objects.equals(step.getStatus(), TaskStep.PAUSED)) {
                // 根据是否有 traceId 判断原等待状态
                if (StringUtils.isNotBlank(step.getTraceId())) {
                    step.setStatus(TaskStep.WAITING);
                } else {
                    step.setStatus(TaskStep.EXECUTING);
                }
                this.taskLogService.record(
                        taskId,
                        step.getId(),
                        TaskLogEventType.STEP_RESUME,
                        " 步骤" + step.getStepName() + "已继续",
                        "system",
                        null);
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
                    "system",
                    null);
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

    public List<TaskStep> getStandardSteps(Template template, Map<String, Object> formDataMap, Map<String, Object> appParams) {
        List<TaskStepDefinition> stepDefs = template.getStepDefinitions();
        List<TaskStep> steps = new ArrayList<>();
        for (int i = 0; i < stepDefs.size(); i++) {
            TaskStepDefinition def = stepDefs.get(i);
            Long operationId = apiService.selectTAppApiById(def.getApiId()).getId();

            // 获取 API 参数原型定义
            Map<String, ApiParamDef> paramDefs = taskParamsService.getApiParamDefs(def.getApiId());

            Map<String, Object> params = new HashMap<>();
            for (ParamBinding binding : def.getParams()) {
                Object value = null;
                switch (binding.getSource()) {
                    case "field":
                        value = formDataMap.get(binding.getValue());
                        break;
                    case "app_param":
                        value = appParams.get(binding.getValue());
                        break;
                    case "fixed":
                        value = binding.getValue();
                        break;
                    case "expression":
                        value = expressionEvaluator.evaluateExpression(binding.getValue(),
                                Map.of("form_data", formDataMap, "app_param", appParams));
                        break;
                }

                // 根据 API 参数定义进行最终类型转换
                ApiParamDef paramDef = paramDefs.get(binding.getName());
                if (value != null && paramDef != null) {
                    value = convertValueByType(value, paramDef.getType(), binding.getName());
                }
                params.put(binding.getName(), value);
            }

            String operationJson = null;
            try {
                operationJson = ParamValidator.objectMapper.writeValueAsString(params);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("步骤参数序列化失败: " + e.getMessage(), e);
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

    private Object convertValueByType(Object value, String type, String fieldName) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }
        String typeLower = type != null ? type.toLowerCase() : "string";
        String valueStr = value.toString().trim();
        try {
            switch (typeLower) {
                case "number":
                case "double":
                case "decimal":
                case "float":
                    return Double.parseDouble(valueStr);
                case "integer":
                case "int":
                case "long":
                case "dynamicselect":
                    return Integer.parseInt(valueStr);
                case "boolean":
                case "bool":
                    return Boolean.parseBoolean(valueStr);
                case "string":
                case "text":
                default:
                    return valueStr;
            }
        } catch (Exception e) {
            throw new TaskmgtException(ReturnNo.FIELD_NOTVALID,
                    new Object[]{fieldName},
                    String.format("字段[%s]类型转换失败: 值'%s'无法转换为类型'%s'", fieldName, valueStr, type));
        }
    }
}
