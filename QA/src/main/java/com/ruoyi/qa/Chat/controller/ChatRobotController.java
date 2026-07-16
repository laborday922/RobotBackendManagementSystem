package com.ruoyi.qa.Chat.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.qa.Chat.dto.RobotChatRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateResponse;
import com.ruoyi.qa.Chat.dto.RobotNavigateRequest;
import com.ruoyi.qa.Chat.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@RestController
@Anonymous
@RequestMapping("/qa/chat")
public class ChatRobotController
{
    private final ChatService chatService;

    public ChatRobotController(ChatService chatService)
    {
        this.chatService = chatService;
    }

    @PostMapping(value = "/robot/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamingResponseBody chatStream(@RequestBody RobotChatRequest body, HttpServletResponse response)
    {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        return outputStream -> chatService.streamRobotChat(body, outputStream);
    }

    @PostMapping("/robot/navigate")
    public AjaxResult navigate(@RequestBody RobotNavigateRequest body)
    {
        RobotNavigateResponse response = chatService.navigateToPoint(body);
        if (!response.isSuccess())
        {
            return AjaxResult.error(response.getMessage()).put("data", response);
        }
        return AjaxResult.success(response);
    }
}
