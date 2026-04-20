package com.ruoyi.taskmgt.service;

import com.ruoyi.taskmgt.service.vo.DynamicParamVo;

import java.util.List;

public interface ITaskParamsService {
    List<DynamicParamVo> getDynamicParams(Long apiId, Long robotId);
}
