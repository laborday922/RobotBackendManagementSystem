package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TaskLogReuseService {
    private final TaskLogRepository taskLogRepository;
    @Async
    public void record(Long taskId, Long stepId, String eventType, String content, String operator, Long tenantId) {
        try {
            TaskLog taskLog = TaskLog.builder()
                    .taskId(taskId)
                    .stepId(stepId)
                    .eventType(eventType)
                    .content(content)
                    .operator(operator)
                    .tenantId(tenantId)
                    .build();
            taskLogRepository.insert(taskLog);
        } catch (Exception e) {
            log.error("记录任务日志失败: taskId={}, eventType={}", taskId, eventType, e);
        }
    }
}
