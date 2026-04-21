package com.ruoyi.taskmgt.controller;

import com.ruoyi.taskmgt.monitor.AsyncOperationMonitor;
import com.ruoyi.taskmgt.monitor.dto.RobotCallbackData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/taskmgt")
@RequiredArgsConstructor
public class RobotCallbackController {

    private final AsyncOperationMonitor asyncOperationMonitor;

    /**
     * 接收机器人主动回调
     * @param callbackData 回调数据
     * @return 响应
     */
    @PostMapping("/callback/robot")
    public ResponseEntity<Void> receiveRobotCallback(@RequestBody RobotCallbackData callbackData) {
        String traceId = callbackData.getTraceId();
        log.info("收到机器人回调, traceId={}, success={}", traceId, callbackData.isSuccess());

        asyncOperationMonitor.handleRobotCallback(traceId, callbackData);

        // 无论处理是否成功，都返回 200 避免机器人重试
        return ResponseEntity.ok().build();
    }
}