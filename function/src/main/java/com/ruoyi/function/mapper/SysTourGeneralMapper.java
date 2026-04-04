package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysTourGeneral;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysTourGeneralMapper {

    SysTourGeneral selectByRobotId(@Param("robotId") Long robotId);

    List<SysTourGeneral> selectList(SysTourGeneral general);

    int insert(SysTourGeneral general);

    int update(SysTourGeneral general);

    int deleteByRobotId(@Param("robotId") Long robotId);
}