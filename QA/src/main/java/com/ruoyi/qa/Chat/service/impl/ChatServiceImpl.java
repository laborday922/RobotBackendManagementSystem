package com.ruoyi.qa.Chat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.service.ISysPointService;
import com.ruoyi.qa.Dify.DifyChatClient;
import com.ruoyi.qa.Chat.dto.DifyChatMessagesRequest;
import com.ruoyi.qa.Chat.dto.RobotChatRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateResponse;
import com.ruoyi.qa.Chat.service.ChatService;
import com.ruoyi.taskmgt.invoker.RobotInvoker;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskRequest;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService
{
    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final DifyChatClient difyChatClient;
    private final ISysPointService sysPointService;
    private final RobotInvoker robotInvoker;

    public ChatServiceImpl(DifyChatClient difyChatClient, ISysPointService sysPointService, RobotInvoker robotInvoker)
    {
        this.difyChatClient = difyChatClient;
        this.sysPointService = sysPointService;
        this.robotInvoker = robotInvoker;
    }

    @Override
    public void streamRobotChat(RobotChatRequest request, OutputStream outputStream) throws IOException
    {
        if (request == null || !StringUtils.hasText(request.getRobotId()))
        {
            writeSseError(outputStream, "robotId is blank");
            return;
        }
        if (!StringUtils.hasText(request.getQuery()))
        {
            writeSseError(outputStream, "query is blank");
            return;
        }

        DifyChatMessagesRequest difyReq = new DifyChatMessagesRequest();
        difyReq.setInputs(Collections.emptyMap());
        difyReq.setFiles(Collections.emptyList());
        difyReq.setQuery(request.getQuery());
        difyReq.setResponseMode("streaming");
        difyReq.setConversationId(StringUtils.hasText(request.getConversationId()) ? request.getConversationId() : "");
        difyReq.setUser(request.getRobotId());

        outputStream.write(":\n\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        log.info("Robot chat stream: robotId={}, conversationId={}, queryLen={}",
            request.getRobotId(),
            request.getConversationId(),
            request.getQuery() == null ? 0 : request.getQuery().length());

        try
        {
            difyChatClient.postChatMessagesStreaming(difyReq, outputStream);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            writeSseError(outputStream, "interrupted");
        }
        catch (Exception e)
        {
            writeSseError(outputStream, e.getMessage());
        }
    }

    @Override
    public RobotNavigateResponse navigateToPoint(RobotNavigateRequest request)
    {
        if (request == null || request.getRobotId() == null)
        {
            return buildError("robotId is blank");
        }
        if (request.getRobotPointId() == null)
        {
            return buildError("robotPointId is blank");
        }
        if (!robotInvoker.isRobotOnline(request.getRobotId()))
        {
            return buildError("robot is offline");
        }

        SysPoint point = findPoint(request.getRobotId(), request.getRobotPointId());
        if (point == null)
        {
            return buildError("point not found for robot");
        }
        if (!"1".equals(point.getStatus()))
        {
            return buildError("point is disabled");
        }

        String traceId = UUID.randomUUID().toString();
        Map<String, Object> params = new HashMap<>();
        params.put("position", String.valueOf(point.getRobotPointId()));

        RobotTaskRequest robotRequest = new RobotTaskRequest();
        robotRequest.setTraceId(traceId);
        robotRequest.setOperationId(2L);
        robotRequest.setParams(params);
        robotRequest.setMode(null);

        RobotTaskResponse response = robotInvoker.execute(request.getRobotId(), robotRequest);
        RobotNavigateResponse result = new RobotNavigateResponse();
        result.setSuccess(response.isSuccess());
        result.setMessage(response.isSuccess() ? "navigate command sent" : normalizeMessage(response.getErrorMsg(), "navigate command failed"));
        result.setRobotId(request.getRobotId());
        result.setRobotPointId(point.getRobotPointId());
        result.setSysPointId(point.getSysPointId());
        result.setPointName(point.getPointName());
        result.setTraceId(StringUtils.hasText(response.getTraceId()) ? response.getTraceId() : traceId);
        result.setMode(response.getMode());
        result.setCompleted(response.isCompleted());
        result.setProgress(response.getProgress());
        result.setStatus(response.getStatus());

        log.info("Robot navigate request: robotId={}, robotPointId={}, sysPointId={}, traceId={}, success={}, mode={}",
            request.getRobotId(), point.getRobotPointId(), point.getSysPointId(), result.getTraceId(), result.isSuccess(), result.getMode());
        return result;
    }

    private static void writeSseError(OutputStream outputStream, String message) throws IOException
    {
        String safe = message == null ? "" : message.replace("\n", " ").replace("\r", " ");
        Map<String, Object> data = new HashMap<>();
        data.put("event", "error");
        data.put("message", safe);
        String payload = "data: " + JSON.toJSONString(data) + "\n\n";
        outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private SysPoint findPoint(Long robotId, Long robotPointId)
    {
        List<SysPoint> pointList = sysPointService.selectByRobotId(robotId);
        if (pointList == null || pointList.isEmpty())
        {
            return null;
        }
        for (SysPoint point : pointList)
        {
            if (point != null && robotPointId.equals(point.getRobotPointId()))
            {
                return point;
            }
        }
        return null;
    }

    private RobotNavigateResponse buildError(String message)
    {
        RobotNavigateResponse response = new RobotNavigateResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    private String normalizeMessage(String message, String defaultMessage)
    {
        return StringUtils.hasText(message) ? message : defaultMessage;
    }
}
