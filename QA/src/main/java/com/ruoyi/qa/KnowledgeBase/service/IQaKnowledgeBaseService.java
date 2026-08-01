package com.ruoyi.qa.KnowledgeBase.service;

import com.ruoyi.qa.KnowledgeBase.domain.QaKnowledgeBase;

import java.util.List;

public interface IQaKnowledgeBaseService
{
    QaKnowledgeBase selectQaKnowledgeBaseById(Long id);

    List<QaKnowledgeBase> selectQaKnowledgeBaseList(QaKnowledgeBase qaKnowledgeBase);

    List<QaKnowledgeBase> selectQaKnowledgeBaseOptions();

    int insertQaKnowledgeBase(QaKnowledgeBase qaKnowledgeBase);

    int updateQaKnowledgeBase(QaKnowledgeBase qaKnowledgeBase);

    int deleteQaKnowledgeBaseByIds(Long[] ids);
}
