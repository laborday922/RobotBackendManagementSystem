package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysRobot;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SysRobotMapper
{
    SysRobot selectSysRobotById(@Param("sysRobot") SysRobot sysRobot);
    List<SysRobot> selectSysRobotList(@Param("sysRobot") SysRobot sysRobot);
    int insertSysRobot(SysRobot sysRobot);
    int updateSysRobot(SysRobot sysRobot);
    int updateRobotMode(@Param("robotId") Long robotId, @Param("modeId") Long modeId, @Param("tenantId") Long tenantId);
    int batchUpdateRobotMode(@Param("robotIds") Long[] robotIds, @Param("modeId") Long modeId, @Param("tenantId") Long tenantId);
    int batchUpdateExtStatus(@Param("robotIds") Long[] robotIds, @Param("tenantId") Long tenantId);
    int deleteSysRobotById(Long robotId);
    int deleteSysRobotByIds(Long[] robotIds);
    int deleteSysRobotPhysically(Long robotId);
    int deleteSysRobotPhysicallyByIds(Long[] robotIds);
    int checkSysRobotExists(@Param("sysRobot") SysRobot sysRobot);
    int saveRobotModeConfig(@Param("robotId") Long robotId, @Param("modeId") Long modeId, @Param("config") String config, @Param("tenantId") Long tenantId);
    String getRobotModeConfig(@Param("robotId") Long robotId, @Param("modeId") Long modeId, @Param("tenantId") Long tenantId);
    int deleteRobotModeConfig(@Param("robotId") Long robotId, @Param("modeId") Long modeId, @Param("tenantId") Long tenantId);
    List<Map<String, Object>> selectRobotModeStats(@Param("tenantId") Long tenantId);
    List<SysRobot> selectRecentModeSwitchRobots(@Param("limit") Integer limit, @Param("tenantId") Long tenantId);
    int updateLastModeSwitchTime(@Param("sysRobot") SysRobot sysRobot);
    int incrementModeSwitchCount(@Param("sysRobot") SysRobot sysRobot);
}