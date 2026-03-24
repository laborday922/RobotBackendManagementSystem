package com.ruoyi.taskmgt.mock.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.mock.http.MockRobotHttpClient;
import com.ruoyi.taskmgt.mock.sdk.MockRobotSDK;
import com.ruoyi.taskmgt.service.operation.OperationHandler;
import com.ruoyi.taskmgt.service.operation.OperationResult;
import com.ruoyi.taskmgt.service.operation.OperationType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 移动操作处理器 - 模拟实现
 * operationId = 1001
 */
@Slf4j
@Component
public class MockMoveOperationHandler implements OperationHandler {

    @Autowired
    private MockRobotHttpClient httpClient;
    @Autowired
    private ObjectMapper objectMapper;

    // 模拟SDK实例缓存
    private final Map<Long, MockRobotSDK> sdkCache = new ConcurrentHashMap<>();

    @Override
    public Long getOperationId() {
        return 1001L;
    }

    @Override
    public String getOperationName() {
        return "模拟移动操作";
    }

    @Override
    public OperationResult execute(TaskStep step, Long robotId, Map<String, Object> params) {
        log.info("[模拟处理器-移动] 执行步骤{}, 机器人{}, 参数: {}", step.getId(), robotId, params);

        try {
            // 解析参数
            MoveParams moveParams = parseParams(params);

            // 根据参数决定使用HTTP还是SDK模式（模拟两种场景）
            boolean useSdk = Boolean.TRUE.equals(params.get("useSdk"));

            if (useSdk) {
                return executeBySDK(robotId, moveParams);
            } else {
                return executeByHttp(robotId, moveParams);
            }

        } catch (Exception e) {
            log.error("[模拟处理器-移动] 执行失败", e);
            return OperationResult.builder()
                    .success(false)
                    .message("模拟执行异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * HTTP模式执行
     */
    private OperationResult executeByHttp(Long robotId, MoveParams params) {
        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "x", params.getX(),
                "y", params.getY(),
                "z", params.getZ(),
                "speed", params.getSpeed(),
                "async", params.isAsync()
        );

        String url = String.format("http://192.168.1.%d:8080/api/v1/move", 10 + robotId);

        ResponseEntity<String> response = httpClient.post(url, null, requestBody);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 解析响应
            Map<String, Object> resp = parseJson(response.getBody());

            if (Boolean.TRUE.equals(params.isAsync())) {
                // 异步模式
                String taskId = (String) resp.get("taskId");
                return OperationResult.builder()
                        .success(true)
                        .type(OperationType.ASYNC)
                        .traceId(taskId)
                        .data(Map.of("estimatedTime", resp.get("estimatedTime")))
                        .message("异步移动任务已提交(HTTP模拟)")
                        .build();
            } else {
                // 同步模式
                return OperationResult.builder()
                        .success(true)
                        .type(OperationType.SYNC)
                        .data(resp)
                        .message("同步移动完成(HTTP模拟)")
                        .build();
            }
        } else {
            return OperationResult.builder()
                    .success(false)
                    .message("HTTP调用失败: " + response.getBody())
                    .build();
        }
    }

    /**
     * SDK模式执行
     */
    private OperationResult executeBySDK(Long robotId, MoveParams params) {
        // 获取或创建模拟SDK实例
        MockRobotSDK sdk = sdkCache.computeIfAbsent(robotId, id ->
                new MockRobotSDK(id, "192.168.1." + (10 + id))
        );

        MockRobotSDK.MoveRequest req = MockRobotSDK.MoveRequest.builder()
                .x(params.getX())
                .y(params.getY())
                .z(params.getZ())
                .speed(params.getSpeed())
                .coordinateType(params.getCoordinateType())
                .build();

        if (params.isAsync()) {
            // 异步SDK调用
            MockRobotSDK.MoveResult result = sdk.asyncMove(req, new MockRobotSDK.MoveCallback() {
                @Override
                public void onProgress(String taskId, int progress, String status) {
                    log.info("[模拟SDK回调] 任务{}进度: {}%", taskId, progress);
                }

                @Override
                public void onComplete(String taskId, Map<String, Object> result) {
                    log.info("[模拟SDK回调] 任务{}完成: {}", taskId, result);
                }

                @Override
                public void onError(String taskId, String errorCode, String message) {
                    log.error("[模拟SDK回调] 任务{}失败: {} - {}", taskId, errorCode, message);
                }
            });

            return OperationResult.builder()
                    .success(result.isSuccess())
                    .type(OperationType.CALLBACK)  // SDK异步走回调模式
                    .traceId(result.getTaskId())
                    .data(Map.of("estimatedTime", result.getEstimatedTime()))
                    .message(result.getMessage())
                    .build();

        } else {
            // 同步SDK调用
            MockRobotSDK.MoveResult result = sdk.syncMove(req, 30000);

            return OperationResult.builder()
                    .success(result.isSuccess())
                    .type(OperationType.SYNC)
                    .data(Map.of(
                            "position", Map.of("x", result.getFinalX(), "y", result.getFinalY(), "z", result.getFinalZ())
                    ))
                    .message(result.getMessage())
                    .build();
        }
    }

    @Override
    public boolean cancel(Long robotId, String traceId) {
        log.info("[模拟处理器-移动] 取消任务: robotId={}, traceId={}", robotId, traceId);

        if (traceId.startsWith("HTTP-")) {
            // HTTP模式取消
            String url = String.format("http://192.168.1.%d:8080/api/v1/task/%s/cancel", 10 + robotId, traceId);
            ResponseEntity<String> resp = httpClient.post(url, null, Map.of());
            return resp.getStatusCode().is2xxSuccessful();
        } else {
            // SDK模式取消
            MockRobotSDK sdk = sdkCache.get(robotId);
            return sdk != null && sdk.cancelTask(traceId);
        }
    }

    @Override
    public boolean validateParams(Map<String, Object> params) {
        return params != null &&
                (params.containsKey("x") || params.containsKey("targetX")) &&
                (params.containsKey("y") || params.containsKey("targetY"));
    }

    // ==================== 参数解析 ====================

    private MoveParams parseParams(Map<String, Object> params) {
        return MoveParams.builder()
                .x(getDouble(params, "x", "targetX", 0.0))
                .y(getDouble(params, "y", "targetY", 0.0))
                .z(getDouble(params, "z", "targetZ", 0.0))
                .speed(getInt(params, "speed", 50))
                .coordinateType(getString(params, "coordinateType", "ABSOLUTE"))
                .async(getBoolean(params, "async", false))
                .useSdk(getBoolean(params, "useSdk", false))
                .build();
    }

    private double getDouble(Map<String, Object> params, String key1, String key2, double defaultVal) {
        Object v = params.get(key1);
        if (v == null) v = params.get(key2);
        return v != null ? Double.parseDouble(v.toString()) : defaultVal;
    }

    private int getInt(Map<String, Object> params, String key, int defaultVal) {
        Object v = params.get(key);
        return v != null ? Integer.parseInt(v.toString()) : defaultVal;
    }

    private String getString(Map<String, Object> params, String key, String defaultVal) {
        Object v = params.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    private boolean getBoolean(Map<String, Object> params, String key, boolean defaultVal) {
        Object v = params.get(key);
        return v != null ? Boolean.parseBoolean(v.toString()) : defaultVal;
    }

    private Map<String, Object> parseJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Data
    @Builder
    private static class MoveParams {
        private double x, y, z;
        private int speed;
        private String coordinateType;
        private boolean async;
        private boolean useSdk;  // 内部测试用，选择调用方式
    }
}