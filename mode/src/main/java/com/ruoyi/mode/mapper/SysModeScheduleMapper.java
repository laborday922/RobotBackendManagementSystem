package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.domain.SysRobot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysModeScheduleMapper
{
    SysModeSchedule selectSysModeScheduleById(@Param("sysModeSchedule") SysModeSchedule sysModeSchedule);
    List<SysModeSchedule> selectSysModeScheduleList(@Param("sysModeSchedule") SysModeSchedule sysModeSchedule);
    int insertSysModeSchedule(SysModeSchedule sysModeSchedule);
    int updateSysModeSchedule(SysModeSchedule sysModeSchedule);
    int deleteSysModeScheduleById(Long scheduleId);
    int deleteSysModeScheduleByIds(Long[] scheduleIds);
    int updateScheduleStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);
    List<SysRobot> selectRobotsByScheduleId(@Param("scheduleId") Long scheduleId, @Param("tenantId") Long tenantId);
    int insertScheduleRobots(@Param("scheduleId") Long scheduleId, @Param("robotIds") Long[] robotIds, @Param("tenantId") Long tenantId);
    int deleteScheduleRobots(Long scheduleId);
    int deleteScheduleRobotsByScheduleIds(Long[] scheduleIds);
}