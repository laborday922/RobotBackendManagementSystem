package com.ruoyi.qa.KnowledgeBase.mapper;

import com.ruoyi.qa.KnowledgeBase.domain.QaKnowledgeBase;

import java.util.List;

public interface QaKnowledgeBaseMapper
{
    QaKnowledgeBase selectQaKnowledgeBaseById(Long id);

    QaKnowledgeBase selectQaKnowledgeBaseByName(String kbName);

    List<QaKnowledgeBase> selectQaKnowledgeBaseList(QaKnowledgeBase qaKnowledgeBase);

    List<QaKnowledgeBase> selectQaKnowledgeBaseOptions();

    int insertQaKnowledgeBase(QaKnowledgeBase qaKnowledgeBase);

    int updateQaKnowledgeBase(QaKnowledgeBase qaKnowledgeBase);

    int deleteQaKnowledgeBaseByIds(Long[] ids);
}
