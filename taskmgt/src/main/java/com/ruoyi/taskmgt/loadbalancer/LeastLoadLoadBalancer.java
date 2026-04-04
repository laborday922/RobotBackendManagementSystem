package com.ruoyi.taskmgt.loadbalancer;

import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class LeastLoadLoadBalancer implements RobotLoadBalancer {
    @Autowired
    private StepRepository stepRepository;

    @Override
    public Long selectRobot(List<Long> robotIds, TaskStep step) {
        if (robotIds == null || robotIds.isEmpty()) return null;
        return robotIds.stream()
                .min(Comparator.comparingLong(this::getCurrentStepCount))
                .orElse(null);
    }

    private long getCurrentStepCount(Long robotId) {
        return stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(robotId,null,
                List.of(TaskStep.EXECUTING, TaskStep.WAITING, TaskStep.WAITING_CALLBACK));
    }
}