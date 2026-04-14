package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysNavConfig;
import org.apache.ibatis.annotations.Param;

public interface SysNavConfigMapper {

    /**
     * 获取当前配置（旧方法，保留兼容）
     */
    SysNavConfig selectCurrent();

    /**
     * 根据机器人ID获取配置
     */
    SysNavConfig selectByRobotId(@Param("robotId") String robotId);

    int insert(SysNavConfig config);

    int update(SysNavConfig config);

    /**
     * 根据机器人ID更新配置
     */
    int updateByRobotId(SysNavConfig config);
}