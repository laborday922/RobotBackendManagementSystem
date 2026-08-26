package com.ruoyi.qa.ChatLog.mapper;

import com.ruoyi.qa.ChatLog.domain.QaLog;

public interface QaLogMapper
{
    QaLog selectQaLogById(Long id);

    int insertQaLog(QaLog qaLog);
}
