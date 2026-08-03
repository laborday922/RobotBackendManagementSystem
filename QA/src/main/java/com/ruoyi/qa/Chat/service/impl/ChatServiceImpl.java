package com.ruoyi.qa.Chat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.service.ISysPointService;
import com.ruoyi.qa.Dify.DifyChatClient;
import com.ruoyi.qa.Chat.dto.DifyChatMessagesRequest;
import com.ruoyi.qa.Chat.dto.RobotChatRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateResponse;
import com.ruoyi.qa.Chat.translate.ChatTranslationProperties;
import com.ruoyi.qa.Chat.translate.ChatTranslationService;
import com.ruoyi.qa.Chat.translate.ChatTranslationService.TranslationPreparation;
import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.service.IQaRobotChatRelService;
import com.ruoyi.qa.Chat.service.ChatService;
import com.ruoyi.taskmgt.invoker.RobotInvoker;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskRequest;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private final IQaRobotChatRelService qaRobotChatRelService;
    private final ChatTranslationService chatTranslationService;
    private final ChatTranslationProperties chatTranslationProperties;

    public ChatServiceImpl(DifyChatClient difyChatClient, ISysPointService sysPointService, RobotInvoker robotInvoker,
        IQaRobotChatRelService qaRobotChatRelService, ChatTranslationService chatTranslationService,
        ChatTranslationProperties chatTranslationProperties)
    {
        this.difyChatClient = difyChatClient;
        this.sysPointService = sysPointService;
        this.robotInvoker = robotInvoker;
        this.qaRobotChatRelService = qaRobotChatRelService;
        this.chatTranslationService = chatTranslationService;
        this.chatTranslationProperties = chatTranslationProperties;
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

        Long robotId = parseRobotId(request.getRobotId());
        if (robotId == null)
        {
            writeSseError(outputStream, "robotId must be numeric");
            return;
        }

        QaChat qaChat = qaRobotChatRelService.selectQaChatByRobotId(robotId);
        if (qaChat == null)
        {
            writeSseError(outputStream, "robot chat config not found");
            return;
        }
        if (!StringUtils.hasText(qaChat.getDifyApiKey()))
        {
            writeSseError(outputStream, "dify api key is blank");
            return;
        }

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("robot_id", request.getRobotId());
        inputs.put("chat_id", qaChat.getId());
        inputs.put("chat_name", qaChat.getChatName());

        TranslationPreparation translationPreparation = chatTranslationService.prepareQuery(request.getQuery());
        DifyChatMessagesRequest difyReq = new DifyChatMessagesRequest();
        difyReq.setInputs(inputs);
        difyReq.setFiles(Collections.emptyList());
        difyReq.setQuery(translationPreparation.getChineseQuery());
        difyReq.setResponseMode("streaming");
        difyReq.setConversationId(StringUtils.hasText(request.getConversationId()) ? request.getConversationId() : "");
        difyReq.setUser(request.getRobotId());

        outputStream.write(":\n\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        log.info("Robot chat stream: robotId={}, chatId={}, conversationId={}, queryLen={}",
            request.getRobotId(),
            qaChat.getId(),
            request.getConversationId(),
            request.getQuery() == null ? 0 : request.getQuery().length());
        log.info("Robot chat translation: configured={}, sourceLanguage={}, sourceLanguageCode={}, answerTranslationRequired={}",
            chatTranslationService.isConfigured(),
            translationPreparation.getSourceLanguage(),
            translationPreparation.getSourceLanguageCode(),
            translationPreparation.isAnswerTranslationRequired());

        try
        {
            if (translationPreparation.isAnswerTranslationRequired())
            {
                try (InputStream upstream = difyChatClient.openChatMessagesStreaming(difyReq, qaChat.getDifyApiKey()))
                {
                    relayTranslatedSse(upstream, outputStream, translationPreparation);
                }
            }
            else
            {
                difyChatClient.postChatMessagesStreaming(difyReq, qaChat.getDifyApiKey(), outputStream);
            }
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

    private void relayTranslatedSse(InputStream upstream, OutputStream downstream, TranslationPreparation preparation)
        throws IOException
    {
        AnswerStreamTranslator translator = new AnswerStreamTranslator(chatTranslationService, chatTranslationProperties, preparation);
        List<String> eventLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(upstream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    handleSseEvent(eventLines, downstream, translator);
                    eventLines.clear();
                    continue;
                }
                eventLines.add(line);
            }
        }

        if (!eventLines.isEmpty())
        {
            handleSseEvent(eventLines, downstream, translator);
        }
        translator.flushRemaining(downstream, null);
    }

    private void handleSseEvent(List<String> eventLines, OutputStream downstream, AnswerStreamTranslator translator)
        throws IOException
    {
        if (eventLines == null || eventLines.isEmpty())
        {
            return;
        }

        StringBuilder dataBuilder = new StringBuilder();
        for (String line : eventLines)
        {
            if (!line.startsWith("data:"))
            {
                continue;
            }
            String value = line.substring(5).trim();
            if (dataBuilder.length() > 0)
            {
                dataBuilder.append('\n');
            }
            dataBuilder.append(value);
        }

        if (dataBuilder.length() == 0)
        {
            forwardRawSse(eventLines, downstream);
            return;
        }

        String rawData = dataBuilder.toString();
        if ("[DONE]".equals(rawData))
        {
            translator.flushRemaining(downstream, null);
            forwardRawSse(eventLines, downstream);
            return;
        }

        JSONObject data;
        try
        {
            data = JSON.parseObject(rawData);
        }
        catch (Exception e)
        {
            forwardRawSse(eventLines, downstream);
            return;
        }

        String answer = data.getString("answer");
        if (answer != null)
        {
            translator.accept(data, answer, downstream);
            return;
        }

        String eventName = data.getString("event");
        if ("message_end".equals(eventName) || "error".equals(eventName))
        {
            translator.flushRemaining(downstream, data);
        }
        forwardRawSse(eventLines, downstream);
    }

    private void forwardRawSse(List<String> eventLines, OutputStream downstream) throws IOException
    {
        for (String line : eventLines)
        {
            downstream.write(line.getBytes(StandardCharsets.UTF_8));
            downstream.write('\n');
        }
        downstream.write('\n');
        downstream.flush();
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

    private Long parseRobotId(String robotId)
    {
        if (!StringUtils.hasText(robotId))
        {
            return null;
        }
        try
        {
            return Long.parseLong(robotId.trim());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static class AnswerStreamTranslator
    {
        private final ChatTranslationService translationService;
        private final TranslationPreparation preparation;
        private final int streamFlushThreshold;
        private final StringBuilder pendingChinese = new StringBuilder();
        private JSONObject lastAnswerEvent;

        private AnswerStreamTranslator(ChatTranslationService translationService,
            ChatTranslationProperties translationProperties, TranslationPreparation preparation)
        {
            this.translationService = translationService;
            this.preparation = preparation;
            this.streamFlushThreshold = translationProperties.getStreamFlushThreshold() > 0
                ? translationProperties.getStreamFlushThreshold()
                : 80;
        }

        private void accept(JSONObject answerEvent, String answerChunk, OutputStream downstream) throws IOException
        {
            this.lastAnswerEvent = answerEvent;
            if (answerChunk == null)
            {
                return;
            }
            pendingChinese.append(answerChunk);
            flushCompletedSegments(downstream, answerEvent, false);
        }

        private void flushRemaining(OutputStream downstream, JSONObject answerEvent) throws IOException
        {
            if (answerEvent != null)
            {
                this.lastAnswerEvent = answerEvent;
            }
            flushCompletedSegments(downstream, this.lastAnswerEvent, true);
        }

        private void flushCompletedSegments(OutputStream downstream, JSONObject answerEvent, boolean flushAll)
            throws IOException
        {
            while (true)
            {
                String segment = extractNextSegment(flushAll);
                if (segment == null)
                {
                    return;
                }
                String translated = translationService.translateAssistantText(segment,
                    preparation.getSourceLanguage(), preparation.getSourceLanguageCode());
                if (translated == null)
                {
                    translated = segment;
                }
                emitAnswerEvent(answerEvent, translated, downstream);
            }
        }

        private String extractNextSegment(boolean flushAll)
        {
            if (pendingChinese.length() == 0)
            {
                return null;
            }

            int boundary = firstBoundaryIndex(pendingChinese);
            if (boundary < 0)
            {
                if (!flushAll && pendingChinese.length() < streamFlushThreshold)
                {
                    return null;
                }
                boundary = pendingChinese.length();
            }

            String segment = pendingChinese.substring(0, boundary);
            pendingChinese.delete(0, boundary);
            return segment;
        }

        private int firstBoundaryIndex(CharSequence text)
        {
            for (int i = 0; i < text.length(); i++)
            {
                char ch = text.charAt(i);
                if (ch == '\n' || ch == '。' || ch == '！' || ch == '？' || ch == '；'
                    || ch == '!' || ch == '?' || ch == ';')
                {
                    return i + 1;
                }
            }
            return -1;
        }

        private void emitAnswerEvent(JSONObject answerEvent, String translatedAnswer, OutputStream downstream)
            throws IOException
        {
            JSONObject payload = new JSONObject();
            if (answerEvent != null)
            {
                payload.putAll(answerEvent);
            }
            payload.put("answer", translatedAnswer);
            String sse = "data: " + payload.toJSONString() + "\n\n";
            downstream.write(sse.getBytes(StandardCharsets.UTF_8));
            downstream.flush();
        }
    }
}
