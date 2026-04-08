//package com.ruoyi.taskmgt.mock.http;
//
//import lombok.Builder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 模拟HTTP客户端 - 模拟调用机器人HTTP接口
// */
//@Slf4j
//@Component
//public class MockRobotHttpClient {
//
//    private final Map<String, MockEndpoint> endpoints = new ConcurrentHashMap<>();
//    private final Random random = new Random();
//
//    public MockRobotHttpClient() {
//        // 初始化模拟端点
//        initMockEndpoints();
//    }
//
//    @Data
//    @Builder
//    public static class MockResponse {
//        private int statusCode;
//        private String body;
//        private long delayMs; // 模拟网络延迟
//    }
//
//    private void initMockEndpoints() {
//        // 移动接口
//        endpoints.put("POST:/api/v1/move", (params, robotId) -> {
//            sleep(100 + random.nextInt(200)); // 100-300ms延迟
//
//            Map<String, Object> req = (Map<String, Object>) params;
//            boolean async = Boolean.TRUE.equals(req.get("async"));
//
//            if (async) {
//                String taskId = "HTTP-MOVE-" + System.currentTimeMillis();
//                return MockResponse.builder()
//                        .statusCode(202)
//                        .body(String.format("{\"taskId\":\"%s\",\"status\":\"ACCEPTED\",\"estimatedTime\":8000}", taskId))
//                        .build();
//            } else {
//                // 模拟10%失败
//                if (random.nextInt(10) == 0) {
//                    return MockResponse.builder()
//                            .statusCode(500)
//                            .body("{\"error\":\"Movement blocked by obstacle\"}")
//                            .build();
//                }
//                return MockResponse.builder()
//                        .statusCode(200)
//                        .body("{\"success\":true,\"arrived\":true,\"position\":{\"x\":10,\"y\":20}}")
//                        .build();
//            }
//        });
//
//        // 查询任务状态
//        endpoints.put("GET:/api/v1/task/{taskId}", (params, robotId) -> {
//            sleep(50 + random.nextInt(100));
//            Map<String, Object> paramMap = (Map<String, Object>) params;
//            Long taskId = (Long) paramMap.get("taskId");
//            int progress = random.nextInt(100);
//            String status = progress >= 100 ? "COMPLETED" : "RUNNING";
//
//            return MockResponse.builder()
//                    .statusCode(200)
//                    .body(String.format("{\"taskId\":\"%s\",\"status\":\"%s\",\"progress\":%d}", taskId, status, progress))
//                    .build();
//        });
//
//        // 取消任务
//        endpoints.put("POST:/api/v1/task/{taskId}/cancel", (params, robotId) -> {
//            return MockResponse.builder()
//                    .statusCode(200)
//                    .body("{\"cancelled\":true}")
//                    .build();
//        });
//
//        // 获取机器人状态
//        endpoints.put("GET:/api/v1/status", (params, robotId) -> {
//            return MockResponse.builder()
//                    .statusCode(200)
//                    .body(String.format("{\"online\":true,\"battery\":%d,\"status\":\"IDLE\"}", 70 + random.nextInt(30)))
//                    .build();
//        });
//
//        // 摄像头控制
//        endpoints.put("POST:/api/v1/camera/capture", (params, robotId) -> {
//            sleep(500 + random.nextInt(500)); // 拍照耗时
//
//            return MockResponse.builder()
//                    .statusCode(200)
//                    .body("{\"success\":true,\"imageUrl\":\"http://mock-server/images/test.jpg\",\"timestamp\":" + System.currentTimeMillis() + "}")
//                    .build();
//        });
//    }
//
//    // 模拟HTTP POST
//    public ResponseEntity<String> post(String url, Map<String, String> headers, Object body) {
//        String path = extractPath(url);
//        String key = "POST:" + path;
//
//        log.info("[模拟HTTP] POST {} | Body: {}", url, body);
//
//        MockEndpoint endpoint = endpoints.get(key);
//        if (endpoint == null) {
//            log.warn("[模拟HTTP] 未找到模拟端点: {}", key);
//            return ResponseEntity.status(404).body("{\"error\":\"Endpoint not found\"}");
//        }
//
//        MockResponse resp = endpoint.handle(body, extractRobotId(url));
//        sleep(resp.delayMs);
//
//        return ResponseEntity.status(resp.statusCode)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(resp.body);
//    }
//
//    // 模拟HTTP GET
//    public ResponseEntity<String> get(String url, Map<String, String> headers) {
//        String path = extractPath(url);
//        String key = "GET:" + path.replaceAll("/\\w+$", "/{taskId}"); // 简化匹配
//
//        log.info("[模拟HTTP] GET {}", url);
//
//        MockEndpoint endpoint = findEndpoint(key, path);
//        if (endpoint == null) {
//            return ResponseEntity.status(404).body("{\"error\":\"Not found\"}");
//        }
//
//        Map<String, Object> params = parseParams(url);
//        MockResponse resp = endpoint.handle(params, extractRobotId(url));
//        sleep(resp.delayMs);
//
//        return ResponseEntity.status(resp.statusCode)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(resp.body);
//    }
//
//    // 模拟HTTP PUT/DELETE...
//
//    private MockEndpoint findEndpoint(String key, String path) {
//        MockEndpoint ep = endpoints.get(key);
//        if (ep != null) return ep;
//
//        // 模糊匹配
//        return endpoints.entrySet().stream()
//                .filter(e -> path.startsWith(e.getKey().replace("GET:", "").replace("POST:", "")))
//                .findFirst()
//                .map(Map.Entry::getValue)
//                .orElse(null);
//    }
//
//    private String extractPath(String url) {
//        // 简化处理，实际用URI解析
//        return url.replaceAll("http://[^/]+", "");
//    }
//
//    private Long extractRobotId(String url) {
//        // 从URL提取robotId，如 http://192.168.1.10:8080/... -> 根据IP映射
//        return 1L; // 简化
//    }
//
//    private Map<String, Object> parseParams(String url) {
//        Map<String, Object> params = new java.util.HashMap<>();
//        if (url.contains("?")) {
//            String query = url.substring(url.indexOf("?") + 1);
//            for (String pair : query.split("&")) {
//                String[] kv = pair.split("=");
//                if (kv.length == 2) {
//                    params.put(kv[0], kv[1]);
//                }
//            }
//        }
//        // 从路径提取taskId等
//        String[] parts = url.split("/");
//        if (parts.length > 0 && parts[parts.length-1].matches("\\d+")) {
//            params.put("taskId", parts[parts.length-1]);
//        }
//        return params;
//    }
//
//    private void sleep(long ms) {
//        try {
//            Thread.sleep(ms);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @FunctionalInterface
//    interface MockEndpoint {
//        MockResponse handle(Object params, Long robotId);
//    }
//}