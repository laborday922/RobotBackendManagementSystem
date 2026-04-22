package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.app.domain.TAppApi;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.*;
import com.ruoyi.taskmgt.service.ITaskParamsService;
import com.ruoyi.taskmgt.utils.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StepReuseServiceTest {

    @Mock private StepRepository stepRepository;
    @Mock private TaskLogReuseService taskLogService;
    @Mock private ITAppApiService apiService;
    @Mock private ITaskParamsService taskParamsService;
    @Mock private ExpressionEvaluator expressionEvaluator;
    @Mock private ObjectMapper objectMapper; // 注意：StepReuseService 中没有直接使用 ObjectMapper，但 ParamValidator.objectMapper 是静态的，需要小心。不过我们测试其他方法时可以忽略。
    @Mock
    private StepWebSocketService stepWebSocketService;
    @InjectMocks
    private StepReuseService stepReuseService;

    private final Long TASK_ID = 1L;
    private List<TaskStep> steps;

    @BeforeEach
    void setUp() {
        steps = new ArrayList<>();
        TaskStep step1 = new TaskStep();
        step1.setId(10L);
        step1.setStatus(TaskStep.EXECUTING);
        step1.setStepName("步骤1");
        TaskStep step2 = new TaskStep();
        step2.setId(20L);
        step2.setStatus(TaskStep.WAITING);
        step2.setStepName("步骤2");
        steps.add(step1);
        steps.add(step2);
        doNothing().when(stepWebSocketService).sendStepChangeToRobot(anyLong(), anyString(), any(TaskStep.class), any(Task.class));
    }

    @Test
    void testPauseStepsByTask() {
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(steps);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());
        Task task = new Task();
        task.setId(TASK_ID);
        List<String> result = stepReuseService.pauseStepsByTask(task);

        assertThat(result).isNotNull();
        for (TaskStep step : steps) {
            assertThat(step.getStatus()).isEqualTo(TaskStep.PAUSED);
            verify(taskLogService).record(eq(TASK_ID), eq(step.getId()), eq(TaskLogEventType.STEP_PAUSE), contains("已暂停"), any(), any());
            verify(stepRepository).update(step);
        }
    }

    @Test
    void testPauseStepsByTaskId_SkipNonExecuting() {
        TaskStep finishedStep = new TaskStep();
        finishedStep.setId(30L);
        finishedStep.setStatus(TaskStep.FINISHED);
        steps.add(finishedStep);
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(steps);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        Task task = new Task();
        task.setId(TASK_ID);
        stepReuseService.pauseStepsByTask(task);

        // finishedStep 状态不应改变
        assertThat(finishedStep.getStatus()).isEqualTo(TaskStep.FINISHED);
        verify(taskLogService, never()).record(eq(TASK_ID), eq(30L), any(), any(), any(), any());
    }

    @Test
    void testContinueStepsByTask() {
        for (TaskStep step : steps) {
            step.setStatus(TaskStep.PAUSED);
            step.setTraceId("trace-" + step.getId());
        }
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(steps);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        Task task = new Task();
        task.setId(TASK_ID);
        stepReuseService.continueStepsByTask(task);

        for (TaskStep step : steps) {
            // 有 traceId 的恢复为 WAITING
            assertThat(step.getStatus()).isEqualTo(TaskStep.WAITING);
            verify(taskLogService).record(eq(TASK_ID), eq(step.getId()), eq(TaskLogEventType.STEP_RESUME), contains("已继续"), any(), any());
        }
    }

    @Test
    void testContinueStepsByTaskId_WithoutTrace() {
        TaskStep step = new TaskStep();
        step.setId(99L);
        step.setStatus(TaskStep.PAUSED);
        step.setTraceId(null);
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(Collections.singletonList(step));
        when(stepRepository.update(step)).thenReturn(Collections.emptyList());
        Task task = new Task();
        task.setId(TASK_ID);
        stepReuseService.continueStepsByTask(task);

        assertThat(step.getStatus()).isEqualTo(TaskStep.EXECUTING);
    }

    @Test
    void testTerminatedStepsByTask() {
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(steps);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());
        Task task = new Task();
        task.setId(TASK_ID);
        stepReuseService.terminatedStepsByTask(task);

        for (TaskStep step : steps) {
            assertThat(step.getStatus()).isEqualTo(TaskStep.TERMINATED);
            verify(taskLogService).record(eq(TASK_ID), eq(step.getId()), eq(TaskLogEventType.STEP_TERMINATE), contains("已终止"), any(), any());
        }
    }

    @Test
    void testCancelStepsByTaskId() {
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(steps);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        stepReuseService.cancelStepsByTaskId(TASK_ID);

        for (TaskStep step : steps) {
            assertThat(step.getStatus()).isEqualTo(TaskStep.NOTSTART);
        }
        verify(taskLogService, never()).record(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetStandardSteps() throws Exception {
        Template template = new Template();
        List<TaskStepDefinition> stepDefs = new ArrayList<>();
        TaskStepDefinition def = new TaskStepDefinition();
        def.setName("步骤A");
        def.setApiId(100L);
        def.setDescription("描述");
        List<ParamBinding> bindings = new ArrayList<>();
        ParamBinding binding = new ParamBinding();
        binding.setSource("field");
        binding.setName("param1");
        binding.setValue("formField");
        bindings.add(binding);
        def.setParams(bindings);
        stepDefs.add(def);

        // 构造正确的 workflow JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> workflowMap = new HashMap<>();
        workflowMap.put("steps", stepDefs);
        String workflowJson = mapper.writeValueAsString(workflowMap);
        template.setWorkflow(workflowJson);

        TAppApi api = new TAppApi();
        api.setId(100L);
        when(apiService.selectTAppApiById(100L)).thenReturn(api);
        when(taskParamsService.getApiParamDefs(100L)).thenReturn(new HashMap<>());
//        when(expressionEvaluator.evaluateExpression(anyString(), anyMap())).thenReturn("exprResult");
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put("formField", "formValue");
        Map<String, Object> appParams = new HashMap<>();

        List<TaskStep> result = stepReuseService.getStandardSteps(template, formDataMap, appParams);

        assertThat(result).hasSize(1);
        TaskStep step = result.get(0);
        assertThat(step.getStepName()).isEqualTo("步骤A");
        assertThat(step.getOperationId()).isEqualTo(100L);
        assertThat(step.getOperationJson()).contains("param1");
        assertThat(step.getOrderNum()).isEqualTo(1);
    }
}