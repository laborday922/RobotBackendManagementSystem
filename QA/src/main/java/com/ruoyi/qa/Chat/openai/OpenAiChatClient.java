package com.ruoyi.qa.Chat.openai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * OpenAI 兼容的流式对话客户端，支持 DeepSeek、豆包等 OpenAI 体系模型。
 */
public class OpenAiChatClient
{
    private static final Logger log = LoggerFactory.getLogger(OpenAiChatClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final String modelName;
    private final int connectTimeoutSeconds;
    private final int requestTimeoutSeconds;
    private final HttpClient httpClient;

    public OpenAiChatClient(String baseUrl, String apiKey, String modelName,
        int connectTimeoutSeconds, int requestTimeoutSeconds)
    {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.connectTimeoutSeconds = connectTimeoutSeconds > 0 ? connectTimeoutSeconds : 10;
        this.requestTimeoutSeconds = requestTimeoutSeconds > 0 ? requestTimeoutSeconds : 300;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(this.connectTimeoutSeconds))
            .build();
    }

    /**
     * 发起流式对话请求，返回 SSE InputStream。
     *
     * @param messages 对话消息列表，每条包含 role 和 content
     */
    public InputStream openChatCompletionsStream(JSONArray messages) throws IOException, InterruptedException
    {
        URI uri = chatCompletionsUri();
        String jsonBody = buildRequestBody(messages);

        log.info("OpenAI streaming request: uri={}, model={}, msgCount={}",
            uri, modelName, messages == null ? 0 : messages.size());

        HttpRequest request = HttpRequest.newBuilder(uri)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey.trim())
            .header("Accept", "text/event-stream")
            .timeout(Duration.ofSeconds(requestTimeoutSeconds))
            .build();

        HttpResponse<InputStream> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        Optional<String> contentType = resp.headers().firstValue("content-type");
        log.info("OpenAI streaming response: status={}, contentType={}", resp.statusCode(), contentType.orElse(null));

        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            byte[] bytes = resp.body().readNBytes(4096);
            String err = new String(bytes, StandardCharsets.UTF_8);
            log.warn("OpenAI /chat/completions failed (status={}): {}", resp.statusCode(), trimForLog(err));
            throw new IOException("OpenAI /chat/completions failed (status=" + resp.statusCode() + "): " + trimForLog(err));
        }
        return resp.body();
    }

    private String buildRequestBody(JSONArray messages)
    {
        JSONObject body = new JSONObject();
        body.put("model", modelName.trim());
        body.put("stream", true);
        body.put("messages", messages);
        return body.toJSONString();
    }

    private URI chatCompletionsUri()
    {
        String url = baseUrl.trim();
        return UriComponentsBuilder.fromHttpUrl(url)
            .path("/chat/completions")
            .build(true)
            .toUri();
    }

    public static JSONArray buildMessages(String systemPrompt, String userMessage)
    {
        JSONArray messages = new JSONArray();
        if (StringUtils.hasText(systemPrompt))
        {
            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt.trim());
            messages.add(sys);
        }
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);
        return messages;
    }

    private static String trimForLog(String s)
    {
        if (s == null)
        {
            return null;
        }
        String v = s.replaceAll("\\s+", " ").trim();
        if (v.length() <= 300)
        {
            return v;
        }
        return v.substring(0, 300) + "...";
    }
}
