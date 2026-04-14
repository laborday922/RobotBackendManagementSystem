package com.ruoyi.taskmgt.monitor.enums;

public enum OperationType {
    SYNC,       // 同步立即返回
    ASYNC,      // 异步提交，需轮询/回调
    CALLBACK    // 长时任务，依赖机器人主动回调
}
