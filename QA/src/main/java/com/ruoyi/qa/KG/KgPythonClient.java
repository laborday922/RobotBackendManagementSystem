package com.ruoyi.qa.KG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class KgPythonClient
{
    private static final Logger log = LoggerFactory.getLogger(KgPythonClient.class);

    private final RestTemplate restTemplate;
    private final KgPythonProperties props;

    public KgPythonClient(RestTemplate restTemplate, KgPythonProperties props)
    {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public KgOkResponse upsert(KgUpsertRequest req)
    {
        return upsert(null, null, req);
    }

    public KgOkResponse upsert(String baseUrl, String token, KgUpsertRequest req)
    {
        URI uri = UriComponentsBuilder.fromHttpUrl(requireBaseUrl(baseUrl))
            .path("/files/upsert")
            .build(true)
            .toUri();

        HttpHeaders headers = buildHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<KgUpsertRequest> entity = new HttpEntity<>(req, headers);
        try
        {
            ResponseEntity<KgOkResponse> resp = restTemplate.exchange(uri, HttpMethod.POST, entity, KgOkResponse.class);
            return resp.getBody();
        }
        catch (RestClientException e)
        {
            log.warn("KG python upsert failed: {}", e.getMessage());
            throw e;
        }
    }

    public KgOkResponse delete(String fileId)
    {
        return delete(null, null, fileId);
    }

    public KgOkResponse delete(String baseUrl, String token, String fileId)
    {
        URI uri = UriComponentsBuilder.fromHttpUrl(requireBaseUrl(baseUrl))
            .path("/files/{fileId}")
            .buildAndExpand(fileId)
            .toUri();

        HttpHeaders headers = buildHeaders(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try
        {
            ResponseEntity<KgOkResponse> resp = restTemplate.exchange(uri, HttpMethod.DELETE, entity, KgOkResponse.class);
            return resp.getBody();
        }
        catch (RestClientException e)
        {
            log.warn("KG python delete failed (fileId={}): {}", fileId, e.getMessage());
            throw e;
        }
    }

    public KgUpsertRequest toUpsertRequest(String fileId, String fileName, String content, Map<String, Object> metadata)
    {
        KgUpsertRequest req = new KgUpsertRequest();
        req.setFileId(fileId);
        req.setFileName(fileName);
        req.setContent(content);
        req.setMetadata(metadata);
        return req;
    }

    private HttpHeaders buildHeaders(String token)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        String resolvedToken = StringUtils.hasText(token) ? token : props.getToken();
        if (StringUtils.hasText(resolvedToken))
        {
            headers.setBearerAuth(resolvedToken.trim());
        }
        return headers;
    }

    private String requireBaseUrl(String baseUrl)
    {
        String resolvedBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl : props.getBaseUrl();
        if (!StringUtils.hasText(resolvedBaseUrl))
        {
            throw new IllegalStateException("kg.python.base-url is blank");
        }
        return resolvedBaseUrl.trim();
    }
}
