package com.ruoyi.qa.ChatManage.mapper;

import com.ruoyi.qa.ChatManage.domain.QaChat;

import java.util.List;

public interface QaChatMapper
{
    QaChat selectQaChatById(Long id);

    List<QaChat> selectQaChatList(QaChat qaChat);

    List<QaChat> selectQaChatOptions();

    int insertQaChat(QaChat qaChat);

    int updateQaChat(QaChat qaChat);

    int deleteQaChatById(Long id);

    int deleteQaChatByIds(Long[] ids);
}
