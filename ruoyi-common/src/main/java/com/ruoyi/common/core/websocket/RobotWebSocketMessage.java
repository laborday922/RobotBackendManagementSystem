package com.ruoyi.common.core.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 通信协议消息体（管理端与机器人共用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotWebSocketMessage {
    /**
     * 消息类型：AUTH, AUTH_SUCCESS, REQUEST, RESPONSE, ASYNC_RESULT, HEARTBEAT, HEARTBEAT_ACK
     */
    private String type;
    /**
     * 机器人ID（认证时必填）
     */
    private Long robotId;
    /**
     * 请求/响应匹配ID（请求时生成，响应时原样返回）
     */
    private String correlationId;
    /**
     * 异步任务追踪ID
     */
    private String traceId;
    /**
     * 操作是否成功（用于异步结果）
     */
    private Boolean success;
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 业务数据（可以是 RobotTaskRequest、RobotTaskResponse、查询结果等）
     */
    private Object data;

    // 工厂方法
    public static RobotWebSocketMessage auth(Long robotId) {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("AUTH");
        msg.setRobotId(robotId);
        return msg;
    }

    public static RobotWebSocketMessage authSuccess() {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("AUTH_SUCCESS");
        return msg;
    }

    public static RobotWebSocketMessage request(String correlationId, Object requestData) {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("REQUEST");
        msg.setCorrelationId(correlationId);
        msg.setData(requestData);
        return msg;
    }

    public static RobotWebSocketMessage close(String reason) {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("CLOSE");
        msg.setErrorMsg(reason);
        return msg;
    }

    public static RobotWebSocketMessage response(String correlationId, Object responseData) {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("RESPONSE");
        msg.setCorrelationId(correlationId);
        msg.setData(responseData);
        return msg;
    }

    public static RobotWebSocketMessage asyncResult(String traceId, boolean success, Object data, String errorMsg) {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("ASYNC_RESULT");
        msg.setTraceId(traceId);
        msg.setSuccess(success);
        msg.setData(data);
        msg.setErrorMsg(errorMsg);
        return msg;
    }

    public static RobotWebSocketMessage heartbeat() {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("HEARTBEAT");
        return msg;
    }

    public static RobotWebSocketMessage heartbeatAck() {
        RobotWebSocketMessage msg = new RobotWebSocketMessage();
        msg.setType("HEARTBEAT_ACK");
        return msg;
    }
}