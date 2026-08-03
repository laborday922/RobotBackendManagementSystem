package com.ruoyi.qa.Chat.translate;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class OpenAiTranslationClient
{
    private static final Logger log = LoggerFactory.getLogger(OpenAiTranslationClient.class);

    private final ChatTranslationProperties props;
    private final HttpClient httpClient;

    public OpenAiTranslationClient(ChatTranslationProperties props)
    {
        this.props = props;
        int connectTimeoutSeconds = props.getConnectTimeoutSeconds() > 0 ? props.getConnectTimeoutSeconds() : 10;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .build();
    }

    public String chat(String systemPrompt, String userPrompt) throws IOException, InterruptedException
    {
        HttpRequest request = buildRequest(systemPrompt, userPrompt);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            log.warn("Translation /chat/completions failed (status={}): {}", response.statusCode(), trimForLog(response.body()));
            throw new IOException("translation request failed (status=" + response.statusCode() + ")");
        }

        JSONObject json = JSON.parseObject(response.body());
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            throw new IOException("translation response choices is empty");
        }

        JSONObject first = choices.getJSONObject(0);
        if (first == null)
        {
            throw new IOException("translation response first choice is empty");
        }

        JSONObject message = first.getJSONObject("message");
        if (message == null)
        {
            throw new IOException("translation response message is empty");
        }

        String content = message.getString("content");
        if (!StringUtils.hasText(content))
        {
            throw new IOException("translation response content is empty");
        }
        return content.trim();
    }

    public boolean isConfigured()
    {
        return props.isEnabled()
            && StringUtils.hasText(props.getBaseUrl())
            && StringUtils.hasText(props.getApiKey())
            && StringUtils.hasText(props.getModel());
    }

    private HttpRequest buildRequest(String systemPrompt, String userPrompt)
    {
        JSONObject body = new JSONObject();
        body.put("model", props.getModel().trim());
        body.put("stream", false);
        body.put("temperature", 0);

        JSONArray messages = new JSONArray();
        messages.add(buildMessage("system", systemPrompt));
        messages.add(buildMessage("user", userPrompt));
        body.put("messages", messages);

        HttpRequest.Builder builder = HttpRequest.newBuilder(chatCompletionUri())
            .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + props.getApiKey().trim());

        int timeoutSeconds = props.getRequestTimeoutSeconds();
        if (timeoutSeconds > 0)
        {
            builder.timeout(Duration.ofSeconds(timeoutSeconds));
        }
        return builder.build();
    }

    private URI chatCompletionUri()
    {
        String baseUrl = props.getBaseUrl().trim();
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/chat/completions")
            .build(true)
            .toUri();
    }

    private JSONObject buildMessage(String role, String content)
    {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private static String trimForLog(String text)
    {
        if (text == null)
        {
            return null;
        }
        String value = text.replaceAll("\\s+", " ").trim();
        if (value.length() <= 300)
        {
            return value;
        }
        return value.substring(0, 300) + "...";
    }
}
