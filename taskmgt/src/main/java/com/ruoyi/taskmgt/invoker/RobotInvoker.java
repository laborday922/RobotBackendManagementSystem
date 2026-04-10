package com.ruoyi.taskmgt.invoker;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskRequest;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import com.ruoyi.taskmgt.invoker.dto.TaskStatusResponse;
import com.ruoyi.taskmgt.websocket.RobotWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RobotInvoker {

    private final RobotWebSocketHandler webSocketHandler;

    /**
     * 执行任务（同步等待响应）
     */
    public RobotTaskResponse execute(Long robotId, RobotTaskRequest request) {
        if (!webSocketHandler.isOnline(robotId)) {
            log.warn("机器人 {} 不在线", robotId);
            RobotTaskResponse errorResp = new RobotTaskResponse();
            errorResp.setSuccess(false);
            errorResp.setErrorMsg("机器人不在线");
            errorResp.setMode("SYNC");
            return errorResp;
        }
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotTaskResponse> future = webSocketHandler.sendAndWait(robotId, request, correlationId, 30);
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("调用机器人失败 robotId={}", robotId, e);
            RobotTaskResponse errorResp = new RobotTaskResponse();
            errorResp.setSuccess(false);
            errorResp.setErrorMsg("调用失败: " + e.getMessage());
            errorResp.setMode("SYNC");
            return errorResp;
        }
    }

    /**
     * 查询异步任务状态
     */
    public TaskStatusResponse queryStatus(Long robotId, String traceId) {
        if (!webSocketHandler.isOnline(robotId)) {
            TaskStatusResponse resp = new TaskStatusResponse();
            resp.setCompleted(false);
            resp.setStatus("ROBOT_OFFLINE");
            return resp;
        }
        Map<String, String> queryReq = Map.of("traceId", traceId, "action", "query");
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotTaskResponse> future = webSocketHandler.sendAndWait(robotId, queryReq, correlationId, 10);
        try {
            RobotTaskResponse response = future.get(10, TimeUnit.SECONDS);
            TaskStatusResponse statusResp = new TaskStatusResponse();
            statusResp.setCompleted(response.isCompleted());
            statusResp.setProgress(response.getProgress());
            statusResp.setStatus(response.getStatus());
            statusResp.setData(response.getData());
            statusResp.setErrorMsg(response.getErrorMsg());
            return statusResp;
        } catch (Exception e) {
            log.error("查询状态失败 robotId={}, traceId={}", robotId, traceId, e);
            TaskStatusResponse resp = new TaskStatusResponse();
            resp.setCompleted(false);
            resp.setStatus("QUERY_ERROR");
            return resp;
        }
    }

    /**
     * 获取机器人是否在线
     */
    public boolean isRobotOnline(Long robotId) {
        return webSocketHandler.isOnline(robotId);
    }
}