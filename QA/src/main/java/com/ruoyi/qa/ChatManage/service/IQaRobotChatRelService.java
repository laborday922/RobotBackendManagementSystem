package com.ruoyi.qa.ChatManage.service;

import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.domain.QaRobotChatRel;

import java.util.List;

public interface IQaRobotChatRelService
{
    QaRobotChatRel selectQaRobotChatRelByRobotId(Long robotId);

    List<QaRobotChatRel> selectQaRobotChatRelList(QaRobotChatRel qaRobotChatRel);

    int insertQaRobotChatRel(QaRobotChatRel qaRobotChatRel);

    int updateQaRobotChatRel(QaRobotChatRel qaRobotChatRel);

    int deleteQaRobotChatRelByRobotIds(Long[] robotIds);

    QaChat selectQaChatByRobotId(Long robotId);
}
