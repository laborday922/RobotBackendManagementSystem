package com.ruoyi.qa.Chat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.service.ISysPointService;
import com.ruoyi.qa.ChatLog.domain.QaLog;
import com.ruoyi.qa.ChatLog.service.IQaLogService;
import com.ruoyi.qa.Chat.dto.DifyChatMessagesRequest;
import com.ruoyi.qa.Chat.dto.RobotChatRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateResponse;
import com.ruoyi.qa.Chat.openai.OpenAiChatClient;
import com.ruoyi.qa.Chat.openai.OpenAiConversationStore;
import com.ruoyi.qa.Chat.translate.ChatTranslationProperties;
import com.ruoyi.qa.Chat.translate.ChatTranslationService;
import com.ruoyi.qa.Chat.translate.ChatTranslationService.TranslationPreparation;
import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.service.IQaRobotChatRelService;
import com.ruoyi.qa.Chat.service.ChatService;
import com.ruoyi.qa.Dify.DifyChatClient;
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
import java.util.Date;
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
    private final OpenAiConversationStore openAiConversationStore;
    private final IQaLogService qaLogService;

    public ChatServiceImpl(DifyChatClient difyChatClient, ISysPointService sysPointService, RobotInvoker robotInvoker,
        IQaRobotChatRelService qaRobotChatRelService, ChatTranslationService chatTranslationService,
        ChatTranslationProperties chatTranslationProperties, OpenAiConversationStore openAiConversationStore, IQaLogService qaLogService)
    {
        this.difyChatClient = difyChatClient;
        this.sysPointService = sysPointService;
        this.robotInvoker = robotInvoker;
        this.qaRobotChatRelService = qaRobotChatRelService;
        this.chatTranslationService = chatTranslationService;
        this.chatTranslationProperties = chatTranslationProperties;
        this.openAiConversationStore = openAiConversationStore;
        this.qaLogService = qaLogService;
    }

    @Override
    public void streamRobotChat(RobotChatRequest request, OutputStream outputStream) throws IOException
    {
        long startMs = System.currentTimeMillis();
        if (request == null || !StringUtils.hasText(request.getRobotId()))
        {
            writeSseError(outputStream, "robotId is blank");
            return;
        }

        Long robotId = parseRobotId(request.getRobotId());
        if (robotId == null)
        {
            writeSseError(outputStream, "robotId must be numeric");
            return;
        }

        ChatLogState logState = new ChatLogState();
        logState.setConversationId(trimToNull(request.getConversationId()));

        QaChat qaChat = null;
        try
        {
            if (!StringUtils.hasText(request.getQuery()))
            {
                logState.markError("query is blank");
                writeSseError(outputStream, logState.getErrorMessage());
                return;
            }

            qaChat = qaRobotChatRelService.selectQaChatByRobotId(robotId);
            if (qaChat == null)
            {
                logState.markError("robot chat config not found");
                writeSseError(outputStream, logState.getErrorMessage());
                return;
            }
            if (!StringUtils.hasText(qaChat.getApiKey()))
            {
                logState.markError("api key is blank");
                writeSseError(outputStream, logState.getErrorMessage());
                return;
            }

            if (qaChat.isDify())
            {
                streamDifyChat(request, outputStream, robotId, qaChat, logState);
            }
            else if (qaChat.isOpenai())
            {
                streamOpenAiChat(request, outputStream, qaChat, logState);
            }
            else
            {
                logState.markError("unsupported chat type: " + qaChat.getChatType());
                writeSseError(outputStream, logState.getErrorMessage());
            }
        }
        finally
        {
            persistChatLog(robotId, qaChat, request, logState, System.currentTimeMillis() - startMs);
        }
    }

    // ==================== Dify 路径 ====================

    private void streamDifyChat(RobotChatRequest request, OutputStream outputStream, Long robotId, QaChat qaChat, ChatLogState logState)
        throws IOException
    {
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

        log.info("Robot chat stream [DIFY]: robotId={}, chatId={}, conversationId={}, queryLen={}",
            request.getRobotId(),
            qaChat.getId(),
            request.getConversationId(),
            request.getQuery() == null ? 0 : request.getQuery().length());
        log.info("Robot chat translation: configured={}, sourceLanguage={}, sourceLanguageCode={}, answerTranslationRequired={}",
            chatTranslationService.isConfigured(),
            translationPreparation.getSourceLanguage(),
            translationPreparation.getSourceLanguageCode(),
            translationPreparation.isAnswerTranslationRequired());

        // 使用 QA 配置中的 baseUrl，若为空则回退到全局配置
        String baseUrl = StringUtils.hasText(qaChat.getBaseUrl()) ? qaChat.getBaseUrl().trim() : null;

        try
        {
            if (translationPreparation.isAnswerTranslationRequired())
            {
                try (InputStream upstream = difyChatClient.openChatMessagesStreaming(difyReq, qaChat.getApiKey(), baseUrl))
                {
                    relayDifySse(upstream, outputStream, translationPreparation, logState);
                }
            }
            else
            {
                try (InputStream upstream = difyChatClient.openChatMessagesStreaming(difyReq, qaChat.getApiKey(), baseUrl))
                {
                    relayDifySse(upstream, outputStream, null, logState);
                }
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logState.markError("interrupted");
            writeSseError(outputStream, logState.getErrorMessage());
        }
        catch (Exception e)
        {
            logState.markError(e.getMessage());
            writeSseError(outputStream, logState.getErrorMessage());
        }
    }

    // ==================== OpenAI 路径 ====================

    private void streamOpenAiChat(RobotChatRequest request, OutputStream outputStream, QaChat qaChat, ChatLogState logState) throws IOException
    {
        String conversationId = request.getConversationId();
        boolean isNewConversation = !StringUtils.hasText(conversationId);

        if (isNewConversation)
        {
            conversationId = openAiConversationStore.createConversation(request.getRobotId());
        }
        logState.setConversationId(conversationId);

        // 追加用户消息到对话历史
        openAiConversationStore.appendUserMessage(conversationId, request.getQuery());

        // 获取完整消息历史
        JSONArray messages = openAiConversationStore.getMessages(conversationId);

        String baseUrl = qaChat.getBaseUrl().trim();
        String apiKey = qaChat.getApiKey().trim();
        String modelName = qaChat.getModelName().trim();

        OpenAiChatClient client = new OpenAiChatClient(baseUrl, apiKey, modelName, 10, 300);

        outputStream.write(":\n\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        log.info("Robot chat stream [OPENAI]: robotId={}, chatId={}, model={}, conversationId={}, isNew={}, queryLen={}",
            request.getRobotId(),
            qaChat.getId(),
            modelName,
            conversationId,
            isNewConversation,
            request.getQuery() == null ? 0 : request.getQuery().length());

        int answerStart = logState.getAnswerCollector().length();

        try (InputStream upstream = client.openChatCompletionsStream(messages))
        {
            relayOpenAiSseToDifyFormat(upstream, outputStream, conversationId, logState.getAnswerCollector());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logState.markError("interrupted");
            writeSseError(outputStream, logState.getErrorMessage());
        }
        catch (Exception e)
        {
            logState.markError(e.getMessage());
            writeSseError(outputStream, logState.getErrorMessage());
        }

        // 存储 assistant 回复到对话历史
        if (logState.getAnswerCollector().length() > answerStart)
        {
            openAiConversationStore.appendAssistantMessage(conversationId, logState.getAnswerCollector().substring(answerStart));
        }
    }

    /**
     * 将 OpenAI SSE 流转换为 Dify 兼容的 SSE 格式输出。
     * OpenAI 格式: data: {"choices":[{"delta":{"content":"..."}}]}
     * Dify 格式:   data: {"event":"message","answer":"..."}
     */
    private void relayOpenAiSseToDifyFormat(InputStream upstream, OutputStream downstream,
        String conversationId, StringBuilder assistantCollector) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(upstream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    continue;
                }
                if (!line.startsWith("data:"))
                {
                    continue;
                }

                String data = line.substring(5).trim();
                if ("[DONE]".equals(data))
                {
                    // 发送 Dify 兼容的 message_end 事件
                    JSONObject endEvent = new JSONObject();
                    endEvent.put("event", "message_end");
                    endEvent.put("conversation_id", conversationId);
                    String sse = "data: " + endEvent.toJSONString() + "\n\n";
                    downstream.write(sse.getBytes(StandardCharsets.UTF_8));
                    downstream.flush();
                    break;
                }

                try
                {
                    JSONObject json = JSON.parseObject(data);
                    JSONArray choices = json.getJSONArray("choices");
                    if (choices == null || choices.isEmpty())
                    {
                        continue;
                    }
                    JSONObject first = choices.getJSONObject(0);
                    if (first == null)
                    {
                        continue;
                    }
                    JSONObject delta = first.getJSONObject("delta");
                    if (delta == null)
                    {
                        continue;
                    }
                    String content = delta.getString("content");
                    if (!StringUtils.hasText(content))
                    {
                        continue;
                    }

                    assistantCollector.append(content);

                    // 包装为 Dify 兼容的 message 事件
                    JSONObject msgEvent = new JSONObject();
                    msgEvent.put("event", "message");
                    msgEvent.put("answer", content);
                    msgEvent.put("conversation_id", conversationId);
                    String sse = "data: " + msgEvent.toJSONString() + "\n\n";
                    downstream.write(sse.getBytes(StandardCharsets.UTF_8));
                    downstream.flush();
                }
                catch (Exception e)
                {
                    log.debug("OpenAI SSE parse skipped: {}", e.getMessage());
                }
            }
        }
    }

    // ==================== Dify 翻译相关（仅 Dify 路径使用） ====================

    private void relayDifySse(InputStream upstream, OutputStream downstream, TranslationPreparation preparation, ChatLogState logState)
        throws IOException
    {
        AnswerStreamTranslator translator = preparation == null ? null
            : new AnswerStreamTranslator(chatTranslationService, chatTranslationProperties, preparation, logState.getAnswerCollector());
        List<String> eventLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(upstream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    handleSseEvent(eventLines, downstream, translator, logState);
                    eventLines.clear();
                    continue;
                }
                eventLines.add(line);
            }
        }

        if (!eventLines.isEmpty())
        {
            handleSseEvent(eventLines, downstream, translator, logState);
        }
        if (translator != null)
        {
            translator.flushRemaining(downstream, null);
        }
    }

    private void handleSseEvent(List<String> eventLines, OutputStream downstream, AnswerStreamTranslator translator, ChatLogState logState)
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
            if (translator != null)
            {
                translator.flushRemaining(downstream, null);
            }
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

        String conversationId = data.getString("conversation_id");
        if (StringUtils.hasText(conversationId) && !StringUtils.hasText(logState.getConversationId()))
        {
            logState.setConversationId(conversationId);
        }

        String answer = data.getString("answer");
        if (answer != null)
        {
            if (translator != null)
            {
                translator.accept(data, answer, downstream);
                return;
            }
            logState.getAnswerCollector().append(answer);
            forwardRawSse(eventLines, downstream);
            return;
        }

        String eventName = data.getString("event");
        if ("error".equals(eventName))
        {
            if (logState.getStatus() == 0)
            {
                String msg = data.getString("message");
                logState.markError(StringUtils.hasText(msg) ? msg : "dify error");
            }
            if (translator != null)
            {
                translator.flushRemaining(downstream, data);
            }
        }
        else if ("message_end".equals(eventName))
        {
            if (translator != null)
            {
                translator.flushRemaining(downstream, data);
            }
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

    // ==================== 导航相关 ====================

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

    // ==================== 工具方法 ====================

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

    // ==================== Dify 翻译内部类 ====================

    private static class AnswerStreamTranslator
    {
        private final ChatTranslationService translationService;
        private final TranslationPreparation preparation;
        private final int streamFlushThreshold;
        private final StringBuilder answerCollector;
        private final StringBuilder pendingChinese = new StringBuilder();
        private JSONObject lastAnswerEvent;

        private AnswerStreamTranslator(ChatTranslationService translationService,
            ChatTranslationProperties translationProperties, TranslationPreparation preparation, StringBuilder answerCollector)
        {
            this.translationService = translationService;
            this.preparation = preparation;
            this.answerCollector = answerCollector;
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
            if (answerCollector != null && translatedAnswer != null)
            {
                answerCollector.append(translatedAnswer);
            }
            downstream.write(sse.getBytes(StandardCharsets.UTF_8));
            downstream.flush();
        }
    }

    private static class ChatLogState
    {
        private final StringBuilder answerCollector = new StringBuilder();
        private String conversationId;
        private int status;
        private String errorMessage;

        private StringBuilder getAnswerCollector()
        {
            return answerCollector;
        }

        private String getConversationId()
        {
            return conversationId;
        }

        private void setConversationId(String conversationId)
        {
            if (StringUtils.hasText(conversationId))
            {
                this.conversationId = conversationId.trim();
            }
        }

        private int getStatus()
        {
            return status;
        }

        private String getErrorMessage()
        {
            return errorMessage;
        }

        private void markError(String message)
        {
            this.status = 1;
            this.errorMessage = sanitizeErrorMessage(message);
        }
    }

    private void persistChatLog(Long robotId, QaChat qaChat, RobotChatRequest request, ChatLogState logState, long durationMs)
    {
        if (robotId == null || request == null)
        {
            return;
        }
        try
        {
            QaLog qaLog = new QaLog();
            qaLog.setRobotId(robotId);
            qaLog.setChatId(qaChat == null ? null : qaChat.getId());
            qaLog.setConversationId(trimToNull(logState.getConversationId()));
            qaLog.setQuery(request.getQuery());
            qaLog.setAnswer(logState.getAnswerCollector().length() == 0 ? null : logState.getAnswerCollector().toString());
            qaLog.setStatus(logState.getStatus());
            qaLog.setErrorMessage(trimToLength(trimToNull(logState.getErrorMessage()), 1000));
            qaLog.setDurationMs(durationMs);
            qaLog.setCreateTime(new Date());
            qaLogService.insertQaLog(qaLog);
        }
        catch (Exception e)
        {
            log.warn("Persist qa_log failed: {}", e.getMessage());
        }
    }

    private static String sanitizeErrorMessage(String message)
    {
        if (message == null)
        {
            return null;
        }
        return message.replace("\n", " ").replace("\r", " ").trim();
    }

    private static String trimToNull(String s)
    {
        if (!StringUtils.hasText(s))
        {
            return null;
        }
        return s.trim();
    }

    private static String trimToLength(String s, int maxLength)
    {
        if (s == null)
        {
            return null;
        }
        if (maxLength <= 0 || s.length() <= maxLength)
        {
            return s;
        }
        return s.substring(0, maxLength);
    }
}
