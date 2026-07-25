package com.ruoyi.qa.ChatManage.mapper;

import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.domain.QaRobotChatRel;

import java.util.List;

public interface QaRobotChatRelMapper
{
    QaRobotChatRel selectQaRobotChatRelByRobotId(Long robotId);

    List<QaRobotChatRel> selectQaRobotChatRelList(QaRobotChatRel qaRobotChatRel);

    int insertQaRobotChatRel(QaRobotChatRel qaRobotChatRel);

    int updateQaRobotChatRel(QaRobotChatRel qaRobotChatRel);

    int deleteQaRobotChatRelByRobotId(Long robotId);

    int deleteQaRobotChatRelByRobotIds(Long[] robotIds);

    QaChat selectQaChatByRobotId(Long robotId);

    int countByChatIds(Long[] chatIds);
}
