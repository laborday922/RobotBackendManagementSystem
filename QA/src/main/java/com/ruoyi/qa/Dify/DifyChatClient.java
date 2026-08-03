package com.ruoyi.qa.Dify;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.qa.Chat.dto.DifyChatMessagesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
public class DifyChatClient
{
    private static final Logger log = LoggerFactory.getLogger(DifyChatClient.class);

    private final DifyChatProperties props;
    private final HttpClient httpClient;

    public DifyChatClient(DifyChatProperties props)
    {
        this.props = props;
        int connectTimeoutSeconds = props.getConnectTimeoutSeconds() > 0 ? props.getConnectTimeoutSeconds() : 10;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .build();
    }

    public String postChatMessagesBlocking(DifyChatMessagesRequest body) throws IOException, InterruptedException
    {
        URI uri = chatMessagesUri();
        HttpRequest request = buildRequest(uri, body, null);
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            log.warn("Dify /chat-messages failed (status={}): {}", resp.statusCode(), trimForLog(resp.body()));
        }
        return resp.body();
    }

    public void postChatMessagesStreaming(DifyChatMessagesRequest body, OutputStream downstream) throws IOException, InterruptedException
    {
        postChatMessagesStreaming(body, null, downstream);
    }

    public void postChatMessagesStreaming(DifyChatMessagesRequest body, String apiKey, OutputStream downstream) throws IOException, InterruptedException
    {
        try (InputStream upstream = openChatMessagesStreaming(body, apiKey))
        {
            byte[] buf = new byte[8192];
            int n;
            boolean loggedFirstChunk = false;
            while ((n = upstream.read(buf)) != -1)
            {
                if (!loggedFirstChunk && n > 0)
                {
                    loggedFirstChunk = true;
                    log.info("Dify streaming first chunk received: bytes={}", n);
                }
                downstream.write(buf, 0, n);
                downstream.flush();
            }
        }
    }

    public InputStream openChatMessagesStreaming(DifyChatMessagesRequest body, String apiKey) throws IOException, InterruptedException
    {
        URI uri = chatMessagesUri();
        log.info("Dify streaming request: uri={}, user={}, conversationId={}, queryLen={}",
            uri,
            body == null ? null : body.getUser(),
            body == null ? null : body.getConversationId(),
            body == null || body.getQuery() == null ? 0 : body.getQuery().length());

        HttpRequest request = buildRequestBuilder(uri, body, apiKey)
            .header("Accept", "text/event-stream")
            .build();

        HttpResponse<InputStream> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        Optional<String> contentType = resp.headers().firstValue("content-type");
        log.info("Dify streaming response: status={}, contentType={}", resp.statusCode(), contentType.orElse(null));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            byte[] bytes = resp.body().readNBytes(4096);
            String err = new String(bytes, StandardCharsets.UTF_8);
            log.warn("Dify /chat-messages streaming failed (status={}): {}", resp.statusCode(), trimForLog(err));
            throw new IOException("Dify /chat-messages streaming failed (status=" + resp.statusCode() + "): " + trimForLog(err));
        }
        return resp.body();
    }

    private HttpRequest buildRequest(URI uri, DifyChatMessagesRequest body, String apiKey)
    {
        return buildRequestBuilder(uri, body, apiKey).build();
    }

    private HttpRequest.Builder buildRequestBuilder(URI uri, DifyChatMessagesRequest body, String apiKey)
    {
        String json = JSON.toJSONString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .header("Content-Type", "application/json");
        int requestTimeoutSeconds = props.getRequestTimeoutSeconds();
        if (requestTimeoutSeconds > 0)
        {
            builder.timeout(Duration.ofSeconds(requestTimeoutSeconds));
        }

        String resolvedApiKey = StringUtils.hasText(apiKey) ? apiKey : props.getApiKey();
        if (StringUtils.hasText(resolvedApiKey))
        {
            builder.header("Authorization", "Bearer " + resolvedApiKey.trim());
        }
        return builder;
    }

    private URI chatMessagesUri()
    {
        String baseUrl = requireBaseUrl();
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/chat-messages")
            .build(true)
            .toUri();
    }

    private String requireBaseUrl()
    {
        String baseUrl = props.getBaseUrl();
        if (!StringUtils.hasText(baseUrl))
        {
            throw new IllegalStateException("dify.base-url is blank");
        }
        return baseUrl.trim();
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
