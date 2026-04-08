//package com.ruoyi.taskmgt.operation;
//
//import com.ruoyi.taskmgt.domain.bo.TaskStep;
//import com.ruoyi.taskmgt.operation.dto.OperationResult;
//
//import java.util.Map;
//
///**
// * 操作处理器接口
// */
//public interface OperationHandler {
//
//    /**
//     * 获取支持的operationId
//     */
//    Long getOperationId();
//
//    /**
//     * 获取操作名称
//     */
//    String getOperationName();
//
//    /**
//     * 执行操作
//     * @param step 步骤信息
//     * @param robotId 目标机器人
//     * @param params 解析后的operationJson参数
//     * @return 执行结果
//     */
//    OperationResult execute(TaskStep step, Long robotId, Map<String, Object> params);
//
//    /**
//     * 取消操作（用于异步任务）
//     */
//    boolean cancel(Long robotId, String traceId);
//
//    /**
//     * 验证参数是否合法
//     */
//    boolean validateParams(Map<String, Object> params);
//}
//
