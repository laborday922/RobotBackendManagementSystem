package com.ruoyi.taskmgt.loadbalancer;

import com.ruoyi.taskmgt.domain.bo.TaskStep;

import java.util.List;

/**
 * 机器人负载均衡器
 */
public interface RobotLoadBalancer {

    /**
     * 从给定的机器人列表中选出一个执行步骤
     * @param robotIds 可用的机器人ID列表（已按状态、在线等过滤）
     * @param step 当前执行的步骤（可用于获取额外信息，如操作类型）
     * @return 选中的机器人ID，若列表为空则返回null
     */
    Long selectRobot(List<Long> robotIds, TaskStep step);
}