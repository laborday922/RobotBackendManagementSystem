package com.ruoyi.qa.Chat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.qa.Dify.DifyClient;
import com.ruoyi.qa.Chat.dto.DifyChatMessagesRequest;
import com.ruoyi.qa.Chat.dto.RobotChatRequest;
import com.ruoyi.qa.Chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService
{
    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final DifyClient difyClient;

    public ChatServiceImpl(DifyClient difyClient)
    {
        this.difyClient = difyClient;
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
            difyClient.postChatMessagesStreaming(difyReq, outputStream);
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
}
