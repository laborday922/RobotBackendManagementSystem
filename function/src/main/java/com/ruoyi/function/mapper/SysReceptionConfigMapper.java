package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysReceptionConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysReceptionConfigMapper {

    SysReceptionConfig selectByRobotId(@Param("robotId") Long robotId);

    List<SysReceptionConfig> selectList(SysReceptionConfig config);

    int insert(SysReceptionConfig config);

    int update(SysReceptionConfig config);

    int deleteByRobotId(@Param("robotId") Long robotId);
}
