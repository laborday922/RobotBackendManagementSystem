package com.ruoyi.taskmgt.listener;

import com.ruoyi.robots.event.RobotWarningEvent;
import com.ruoyi.taskmgt.service.trigger.TaskTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RobotWarningEventListener {
    private final TaskTrigger taskTrigger;

    @EventListener
    public void handleRobotWarning(RobotWarningEvent event) {
        log.info("接收到机器人预警事件：robotId={}, warningType={}, status={}",
                event.getRobotId(), event.getWarningType(), event.getStatus());
        taskTrigger.handleRobotWarning(event);
    }
}