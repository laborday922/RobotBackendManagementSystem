package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysTourContent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysTourContentMapper {

    SysTourContent selectById(@Param("contentId") Long contentId);

    List<SysTourContent> selectByRobotId(@Param("robotId") Long robotId);

    List<SysTourContent> selectList(SysTourContent content);

    int insert(SysTourContent content);

    int update(SysTourContent content);

    int deleteById(@Param("contentId") Long contentId);

    int deleteByRobotId(@Param("robotId") Long robotId);
}
