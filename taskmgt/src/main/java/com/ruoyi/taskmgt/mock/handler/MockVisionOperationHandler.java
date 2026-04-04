package com.ruoyi.taskmgt.mock.handler;

import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.mock.sdk.MockRobotSDK;
import com.ruoyi.taskmgt.operation.OperationHandler;
import com.ruoyi.taskmgt.operation.model.OperationResult;
import com.ruoyi.taskmgt.operation.enums.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 视觉识别操作处理器 - 模拟实现
 * operationId = 1003
 */
@Slf4j
@Component
public class MockVisionOperationHandler implements OperationHandler {

    private final Map<Long, MockRobotSDK> sdkCache = new ConcurrentHashMap<>();

    @Override
    public Long getOperationId() {
        return 1003L;
    }

    @Override
    public String getOperationName() {
        return "模拟视觉识别";
    }

    @Override
    public OperationResult execute(TaskStep step, Long robotId, Map<String, Object> params) {
        log.info("[模拟处理器-视觉] 执行步骤{}, 机器人{}, 参数: {}", step.getId(), robotId, params);

        try {
            MockRobotSDK sdk = sdkCache.computeIfAbsent(robotId, id ->
                    new MockRobotSDK(id, "192.168.1." + (10 + id))
            );

            String algorithm = getString(params, "algorithmType", "object_detection");
            int timeout = getInt(params, "timeout", 5000);

            MockRobotSDK.VisionResult result = sdk.recognize(algorithm, timeout);

            // 转换结果
            List<Map<String, Object>> objects = result.getObjects().stream()
                    .map(obj -> Map.<String, Object>of(
                            "type", obj.getType(),
                            "confidence", obj.getConfidence(),
                            "bbox", List.of(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight())
                    ))
                    .collect(Collectors.toList());

            return OperationResult.builder()
                    .success(result.isRecognized())
                    .type(OperationType.SYNC)
                    .data(Map.of(
                            "recognized", result.isRecognized(),
                            "objects", objects,
                            "imageUrl", "http://mock-server/temp/" + step.getId() + ".jpg"
                    ))
                    .message(result.isRecognized() ? "识别到" + objects.size() + "个目标" : "未识别到目标")
                    .build();

        } catch (Exception e) {
            return OperationResult.builder()
                    .success(false)
                    .message("视觉识别异常: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean cancel(Long robotId, String traceId) {
        return false; // 同步操作
    }

    @Override
    public boolean validateParams(Map<String, Object> params) {
        return true; // 视觉参数都是可选的
    }

    private String getString(Map<String, Object> params, String key, String defaultVal) {
        Object v = params.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    private int getInt(Map<String, Object> params, String key, int defaultVal) {
        Object v = params.get(key);
        return v != null ? Integer.parseInt(v.toString()) : defaultVal;
    }
}