package com.ruoyi.taskmgt.invoker;

import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.invoker.dto.TaskStatusResponse;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskRequest;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class RobotInvoker {
    private final RestTemplate restTemplate;
    private final IRobotsService robotService;

    @Value("${robot.api.token:}")
    private String apiToken;

    public RobotTaskResponse execute(Long robotId, RobotTaskRequest request) {
//        Robot robot = robotService.selectRobotsById(robotId);
//        String robotUrl = robot.getRobotBaseUrl();
        String robotUrl = getMockUrl(robotId);
        String url = robotUrl + "/api/task/execute"; //机器人端任务执行api
        HttpEntity<RobotTaskRequest> entity = new HttpEntity<>(request, buildHeaders());
        ResponseEntity<RobotTaskResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, RobotTaskResponse.class);
        return response.getBody();
    }

    public TaskStatusResponse queryStatus(Long robotId, String traceId) {
//        Robot robot = robotService.selectRobotsById(robotId);
//        String robotUrl = robot.getRobotBaseUrl();
        String robotUrl = getMockUrl(robotId);
        String url = robotUrl + "/api/task/status/" + traceId;
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders());
        ResponseEntity<TaskStatusResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, TaskStatusResponse.class);
        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        return headers;
    }

    private String getMockUrl(Long robotId){
        return "https://"+robotId;
    }
}