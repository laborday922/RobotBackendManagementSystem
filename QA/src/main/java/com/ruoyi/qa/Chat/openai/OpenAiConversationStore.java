package com.ruoyi.qa.Chat.openai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenAI 对话历史内存存储。
 * 因为 OpenAI 体系没有服务端 conversation 概念，需要在请求时带上历史消息。
 * 每个机器人只保留一个活跃对话，新对话自动清理旧对话。
 */
@Component
public class OpenAiConversationStore
{
    private static final Logger log = LoggerFactory.getLogger(OpenAiConversationStore.class);

    /** 每个 conversation 最多保留的消息条数（system + 最近 N 轮 user/assistant） */
    private static final int MAX_MESSAGES = 20;

    /** conversationId → 消息列表 */
    private final Map<String, JSONArray> store = new ConcurrentHashMap<>();

    /** robotId → conversationId，用于新对话自动清理旧对话 */
    private final Map<String, String> robotConversationMap = new ConcurrentHashMap<>();

    /**
     * 为指定机器人创建新对话，自动清理该机器人的旧对话。
     */
    public String createConversation(String robotId)
    {
        // 清理旧对话
        String oldConversationId = robotConversationMap.remove(robotId);
        if (oldConversationId != null)
        {
            store.remove(oldConversationId);
            log.info("OpenAI old conversation cleaned: robotId={}, conversationId={}", robotId, oldConversationId);
        }

        String conversationId = UUID.randomUUID().toString().replace("-", "");
        store.put(conversationId, new JSONArray());
        robotConversationMap.put(robotId, conversationId);
        log.info("OpenAI conversation created: robotId={}, conversationId={}", robotId, conversationId);
        return conversationId;
    }

    public JSONArray getMessages(String conversationId)
    {
        JSONArray messages = store.get(conversationId);
        return messages != null ? messages : new JSONArray();
    }

    public void appendUserMessage(String conversationId, String content)
    {
        JSONArray messages = store.get(conversationId);
        if (messages == null)
        {
            return;
        }
        JSONObject msg = new JSONObject();
        msg.put("role", "user");
        msg.put("content", content);
        messages.add(msg);
        trimIfNeeded(messages);
    }

    public void appendAssistantMessage(String conversationId, String content)
    {
        JSONArray messages = store.get(conversationId);
        if (messages == null)
        {
            return;
        }
        JSONObject msg = new JSONObject();
        msg.put("role", "assistant");
        msg.put("content", content);
        messages.add(msg);
        trimIfNeeded(messages);
    }

    public void removeConversation(String conversationId)
    {
        store.remove(conversationId);
    }

    private void trimIfNeeded(JSONArray messages)
    {
        while (messages.size() > MAX_MESSAGES)
        {
            int removeIndex = 0;
            JSONObject first = messages.getJSONObject(0);
            if ("system".equals(first.getString("role")))
            {
                removeIndex = 1;
            }
            if (removeIndex < messages.size())
            {
                messages.remove(removeIndex);
            }
        }
    }
}
