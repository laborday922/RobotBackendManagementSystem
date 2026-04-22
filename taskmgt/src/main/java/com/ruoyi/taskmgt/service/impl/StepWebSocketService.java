package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.invoker.RobotInvoker;
import com.ruoyi.taskmgt.monitor.AsyncOperationMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StepWebSocketService {
    private final RobotInvoker robotInvoker;
    private final AsyncOperationMonitor asyncMonitor;
    private final StepExecutionService stepExecutionService;
    public void sendStepChangeToRobot(Long robotId, String command, TaskStep step, Task task){
        switch (command){
            case "pause":{


                try {
                    Map<String, Object> cmd = Map.of("action", command, "traceId", step.getTraceId(), "stepId", step.getId());
                    robotInvoker.sendCommand(robotId, cmd, 5);
                } catch (Exception e) {
                    log.warn("已将管理端步骤状态置为暂停。向机器人发送暂停指令失败: {}", e.getMessage());
                }

                // 取消异步轮询任务
                if (step.getTraceId() != null) {
                    asyncMonitor.cancelPolling(step.getTraceId());
                }
            } break;
            case "continue":{
                try {
                    Map<String, Object> cmd = Map.of("action", command, "traceId", step.getTraceId(), "stepId", step.getId());
                    robotInvoker.sendCommand(robotId, cmd, 5);
                    stepExecutionService.sendAndWait(robotId,step);
                } catch (Exception e) {
                    log.warn("已将管理端步骤恢复允许状态。向机器人发送恢复指令失败: {}", e.getMessage());
                }
            }break;
            case "terminate":{
                try {
                    Map<String, Object> cmd = Map.of("action", command, "traceId", step.getTraceId(), "stepId", step.getId());
                    robotInvoker.sendCommand(robotId, cmd, 5);
                } catch (Exception e) {
                    log.warn("已将管理端步骤状态置为终止。向机器人发送终止指令失败: {}", e.getMessage());
                }
                if (step.getTraceId() != null) {
                    asyncMonitor.cancelPolling(step.getTraceId());
                }
            }
        }
    }
}
