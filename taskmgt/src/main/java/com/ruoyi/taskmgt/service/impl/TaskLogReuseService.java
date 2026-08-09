package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskLogReuseService {
    private final TaskLogRepository taskLogRepository;

    /** 任务ID -> 当前执行交互ID 内存映射，供步骤执行时查询 */
    private final Map<Long, String> taskInteractionIds = new ConcurrentHashMap<>();

    /**
     * 记录任务日志（不含 interactionId，兼容旧调用）
     */
    @Async
    public void record(Long taskId, Long stepId, String eventType, String content, String operator, Long tenantId) {
        String interactionId = taskInteractionIds.get(taskId);
        record(taskId, stepId, eventType, content, operator, tenantId, interactionId);
    }

    /**
     * 记录任务日志（含 interactionId）
     */
    @Async
    public void record(Long taskId, Long stepId, String eventType, String content, String operator, Long tenantId, String interactionId) {
        try {
            TaskLog taskLog = TaskLog.builder()
                    .taskId(taskId)
                    .stepId(stepId)
                    .eventType(eventType)
                    .content(content)
                    .operator(operator)
                    .tenantId(tenantId)
                    .interactionId(interactionId)
                    .build();
            taskLogRepository.insert(taskLog);
            // TASK_COMPLETE / TASK_TERMINATE 时清理内存映射
            if (TaskLogEventType.TASK_COMPLETE.equals(eventType) || TaskLogEventType.TASK_TERMINATE.equals(eventType)) {
                taskInteractionIds.remove(taskId);
                log.info("任务 {} 交互结束, 清理interactionId", taskId);
            }
        } catch (Exception e) {
            log.error("记录任务日志失败: taskId={}, eventType={}", taskId, eventType, e);
        }
    }

    /**
     * 生成新的交互唯一标识
     */
    public String generateInteractionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 开始一次新的任务交互，生成并记录 interactionId
     */
    public String startInteraction(Long taskId) {
        String interactionId = generateInteractionId();
        taskInteractionIds.put(taskId, interactionId);
        log.info("任务 {} 开始新交互, interactionId={}", taskId, interactionId);
        return interactionId;
    }

    /**
     * 获取当前任务执行的 interactionId（供步骤执行时查询）
     */
    public String getInteractionId(Long taskId) {
        return taskInteractionIds.get(taskId);
    }
}
