package com.ruoyi.taskmgt.service;


import com.ruoyi.taskmgt.domain.bo.Template;
import com.ruoyi.taskmgt.service.vo.TemplateVo;

import java.util.List;

public interface ITemplateService {
    TemplateVo createTemplate(Template template);

    void updateTemplate(Template template);

    void deleteTemplate(Long id);

    void banTemplate(Long id);

    void resumeTemplate(Long id);

    List<TemplateVo> retrieveTemplates(Long appId, String name, Byte status, Long robotGroupId);

    TemplateVo getTemplate(Long id);
}
