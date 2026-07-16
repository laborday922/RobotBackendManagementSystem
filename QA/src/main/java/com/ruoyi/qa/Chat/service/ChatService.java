package com.ruoyi.qa.Chat.service;

import com.ruoyi.qa.Chat.dto.RobotChatRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateRequest;
import com.ruoyi.qa.Chat.dto.RobotNavigateResponse;

import java.io.IOException;
import java.io.OutputStream;

public interface ChatService
{
    void streamRobotChat(RobotChatRequest request, OutputStream outputStream) throws IOException;

    RobotNavigateResponse navigateToPoint(RobotNavigateRequest request);
}
