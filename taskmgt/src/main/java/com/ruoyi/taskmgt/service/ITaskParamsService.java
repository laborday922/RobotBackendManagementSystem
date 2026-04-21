package com.ruoyi.taskmgt.service;

import com.ruoyi.taskmgt.domain.bo.ApiParamDef;
import com.ruoyi.taskmgt.service.vo.DynamicParamVo;

import java.util.List;
import java.util.Map;

public interface ITaskParamsService {
    Map<String, ApiParamDef> getApiParamDefs(Long apiId);

    List<DynamicParamVo> getDynamicParams(Long apiId, Long robotId);
}
