package com.ruoyi.qa.Dify;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.qa.Dify.dto.DifyDocumentByTextRequest;
import com.ruoyi.qa.Dify.dto.DifyDocumentUpsertResponse;
import com.ruoyi.qa.Dify.dto.DifyListDocumentsResponse;
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
public class DifyDatasetClient
{
    private static final Logger log = LoggerFactory.getLogger(DifyDatasetClient.class);

    private final DifyChatProperties props;
    private final HttpClient httpClient;

    public DifyDatasetClient(DifyChatProperties props)
    {
        this.props = props;
        int connectTimeoutSeconds = props.getConnectTimeoutSeconds() > 0 ? props.getConnectTimeoutSeconds() : 10;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .build();
    }

    public DifyDocumentUpsertResponse createDocumentByText(String name, String text) throws IOException, InterruptedException
    {
        URI uri = createByTextUri();
        DifyDocumentByTextRequest body = defaultByTextRequest(name, text);
        HttpResponse<String> resp = httpClient.send(buildRequest(uri, body), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        log.info("Dify create_by_text response: status={}, body={}", resp.statusCode(), trimForLog(resp.body()));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            throw new IOException("Dify create_by_text failed (status=" + resp.statusCode() + "): " + trimForLog(resp.body()));
        }
        return JSON.parseObject(resp.body(), DifyDocumentUpsertResponse.class);
    }

    public DifyDocumentUpsertResponse updateDocumentByText(String documentId, String name, String text) throws IOException, InterruptedException
    {
        if (!StringUtils.hasText(documentId))
        {
            throw new IllegalArgumentException("documentId is blank");
        }
        URI uri = updateByTextUri(documentId.trim());
        DifyDocumentByTextRequest body = defaultByTextRequest(name, text);
        HttpResponse<String> resp = httpClient.send(buildRequest(uri, body), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        log.info("Dify update-by-text response: status={}, body={}", resp.statusCode(), trimForLog(resp.body()));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            throw new IOException("Dify update-by-text failed (status=" + resp.statusCode() + "): " + trimForLog(resp.body()));
        }
        return JSON.parseObject(resp.body(), DifyDocumentUpsertResponse.class);
    }

    public void deleteDocument(String documentId) throws IOException, InterruptedException
    {
        if (!StringUtils.hasText(documentId))
        {
            return;
        }
        URI uri = deleteDocumentUri(documentId.trim());
        HttpRequest request = baseRequestBuilder(uri)
            .DELETE()
            .build();
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            log.warn("Dify delete document failed (status={}): {}", resp.statusCode(), trimForLog(resp.body()));
        }
        else
        {
            log.info("Dify delete document ok: status={}, body={}", resp.statusCode(), trimForLog(resp.body()));
        }
    }

    public DifyListDocumentsResponse listDocuments(int page, int limit) throws IOException, InterruptedException
    {
        URI uri = listDocumentsUri(page, limit);
        HttpRequest request = baseRequestBuilder(uri)
            .GET()
            .build();
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        log.info("Dify list documents response: status={}, body={}", resp.statusCode(), trimForLog(resp.body()));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300)
        {
            throw new IOException("Dify list documents failed (status=" + resp.statusCode() + "): " + trimForLog(resp.body()));
        }
        return JSON.parseObject(resp.body(), DifyListDocumentsResponse.class);
    }

    private DifyDocumentByTextRequest defaultByTextRequest(String name, String text)
    {
        DifyDocumentByTextRequest req = new DifyDocumentByTextRequest();
        req.setName(name);
        req.setText(text);
        req.setIndexingTechnique("high_quality");
        req.setDocForm("qa_model");
        req.setDocLanguage("Chinese Simplified");
//        req.setDocForm("hierarchical_model");
//        req.setDocLanguage("Chinese Simplified");

//        DifyDocumentByTextRequest.PreProcessingRuleItem r1 = new DifyDocumentByTextRequest.PreProcessingRuleItem();
//        r1.setId("remove_extra_spaces");
//        r1.setEnabled(true);
//        DifyDocumentByTextRequest.PreProcessingRuleItem r2 = new DifyDocumentByTextRequest.PreProcessingRuleItem();
//        r2.setId("remove_urls_emails");
//        r2.setEnabled(false);
//
//        DifyDocumentByTextRequest.Segmentation parentSeg = new DifyDocumentByTextRequest.Segmentation();
//        parentSeg.setSeparator("\n\n");
//        parentSeg.setMaxTokens(10000);
//        parentSeg.setChunkOverlap(0);
//
//        DifyDocumentByTextRequest.Segmentation childSeg = new DifyDocumentByTextRequest.Segmentation();
//        childSeg.setSeparator("\n\n");
//        childSeg.setMaxTokens(1024);
//        childSeg.setChunkOverlap(0);
//
//        DifyDocumentByTextRequest.Rules rules = new DifyDocumentByTextRequest.Rules();
//        rules.setPreProcessingRules(new DifyDocumentByTextRequest.PreProcessingRuleItem[] { r1, r2 });
//        rules.setSegmentation(parentSeg);
//        rules.setParentMode("full-doc");
//        rules.setSubchunkSegmentation(childSeg);
//
//        DifyDocumentByTextRequest.ProcessRule rule = new DifyDocumentByTextRequest.ProcessRule();
//        rule.setMode("custom");
//        rule.setRules(rules);
//        req.setProcessRule(rule);
        return req;
    }

    private HttpRequest buildRequest(URI uri, Object body)
    {
        String json = JSON.toJSONString(body);
        return baseRequestBuilder(uri)
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .header("Content-Type", "application/json")
            .build();
    }

    private HttpRequest.Builder baseRequestBuilder(URI uri)
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
        int requestTimeoutSeconds = props.getRequestTimeoutSeconds();
        if (requestTimeoutSeconds > 0)
        {
            builder.timeout(Duration.ofSeconds(requestTimeoutSeconds));
        }

        String apiKey = requireDatasetApiKey();
        builder.header("Authorization", "Bearer " + apiKey);
        return builder;
    }

    private URI createByTextUri()
    {
        return UriComponentsBuilder.fromHttpUrl(requireBaseUrl())
            .path("/datasets/")
            .path(requireDatasetId())
            .path("/document/create-by-text")
            .build(true)
            .toUri();
    }

    private URI updateByTextUri(String documentId)
    {
        return UriComponentsBuilder.fromHttpUrl(requireBaseUrl())
            .path("/datasets/")
            .path(requireDatasetId())
            .path("/documents/")
            .path(documentId)
            .path("/update-by-text")
            .build(true)
            .toUri();
    }

    private URI deleteDocumentUri(String documentId)
    {
        return UriComponentsBuilder.fromHttpUrl(requireBaseUrl())
            .path("/datasets/")
            .path(requireDatasetId())
            .path("/documents/")
            .path(documentId)
            .build(true)
            .toUri();
    }

    private URI listDocumentsUri(int page, int limit)
    {
        return UriComponentsBuilder.fromHttpUrl(requireBaseUrl())
            .path("/datasets/")
            .path(requireDatasetId())
            .path("/documents")
            .queryParam("page", Math.max(page, 1))
            .queryParam("limit", Math.max(limit, 1))
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

    private String requireDatasetId()
    {
        String datasetId = props.getDatasetId();
        if (!StringUtils.hasText(datasetId))
        {
            throw new IllegalStateException("dify.dataset-id is blank");
        }
        return datasetId.trim();
    }

    private String requireDatasetApiKey()
    {
        String apiKey = props.getDatasetApiKey();
        if (!StringUtils.hasText(apiKey))
        {
            throw new IllegalStateException("dify.dataset-api-key is blank");
        }
        return apiKey.trim();
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
