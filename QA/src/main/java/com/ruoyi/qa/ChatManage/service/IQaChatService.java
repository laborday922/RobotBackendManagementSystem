package com.ruoyi.qa.ChatManage.service;

import com.ruoyi.qa.ChatManage.domain.QaChat;

import java.util.List;

public interface IQaChatService
{
    QaChat selectQaChatById(Long id);

    List<QaChat> selectQaChatList(QaChat qaChat);

    List<QaChat> selectQaChatOptions();

    int insertQaChat(QaChat qaChat);

    int updateQaChat(QaChat qaChat);

    int deleteQaChatByIds(Long[] ids);
}
