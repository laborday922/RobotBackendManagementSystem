package com.ruoyi.qa.Chat.service;

import com.ruoyi.qa.Chat.dto.RobotChatRequest;

import java.io.IOException;
import java.io.OutputStream;

public interface ChatService
{
    void streamRobotChat(RobotChatRequest request, OutputStream outputStream) throws IOException;
}

