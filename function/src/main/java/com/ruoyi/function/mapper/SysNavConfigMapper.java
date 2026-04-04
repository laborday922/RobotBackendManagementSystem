package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysNavConfig;

public interface SysNavConfigMapper {

    SysNavConfig selectCurrent();

    int insert(SysNavConfig config);

    int update(SysNavConfig config);
}
