package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysRobot;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 机器人模式扩展Mapper接口
 * 注意：基础信息请使用 IRobotsService
 *
 * @author ruoyi
 */
public interface SysRobotMapper
{
    /**
     * 查询机器人模式扩展信息
     *
     * @param robotId 机器人ID
     * @return 机器人模式扩展信息
     */
    public SysRobot selectSysRobotById(Long robotId);

    /**
     * 查询机器人模式扩展信息列表
     *
     * @param sysRobot 机器人模式扩展信息
     * @return 机器人模式扩展信息集合
     */
    public List<SysRobot> selectSysRobotList(SysRobot sysRobot);

    /**
     * 新增机器人模式扩展信息
     *
     * @param sysRobot 机器人模式扩展信息
     * @return 结果
     */
    public int insertSysRobot(SysRobot sysRobot);

    /**
     * 修改机器人模式扩展信息
     *
     * @param sysRobot 机器人模式扩展信息
     * @return 结果
     */
    public int updateSysRobot(SysRobot sysRobot);

    /**
     * 更新机器人当前模式
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 结果
     */
    public int updateRobotMode(@Param("robotId") Long robotId, @Param("modeId") Long modeId);

    /**
     * 批量更新机器人模式
     *
     * @param robotIds 机器人ID数组
     * @param modeId 模式ID
     * @return 结果
     */
    public int batchUpdateRobotMode(@Param("robotIds") Long[] robotIds, @Param("modeId") Long modeId);

    /**
     * 批量更新机器人扩展信息状态
     *
     * @param robotIds 机器人ID数组
     * @param status 状态（仅用于扩展表）
     * @return 结果
     */
    public int batchUpdateExtStatus(@Param("robotIds") Long[] robotIds, @Param("status") Integer status);

    /**
     * 删除机器人模式扩展信息（逻辑删除）
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int deleteSysRobotById(Long robotId);

    /**
     * 批量删除机器人模式扩展信息（逻辑删除）
     *
     * @param robotIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysRobotByIds(Long[] robotIds);

    /**
     * 物理删除机器人模式扩展信息（慎用）
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int deleteSysRobotPhysically(Long robotId);

    /**
     * 批量物理删除机器人模式扩展信息（慎用）
     *
     * @param robotIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysRobotPhysicallyByIds(Long[] robotIds);

    /**
     * 检查机器人是否存在扩展信息
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int checkSysRobotExists(Long robotId);

    /**
     * 保存机器人模式配置（JSON格式）
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @param config 配置JSON字符串
     * @return 结果
     */
    public int saveRobotModeConfig(@Param("robotId") Long robotId,
                                   @Param("modeId") Long modeId,
                                   @Param("config") String config);

    /**
     * 获取机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 配置JSON字符串
     */
    public String getRobotModeConfig(@Param("robotId") Long robotId, @Param("modeId") Long modeId);

    /**
     * 删除机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 结果
     */
    public int deleteRobotModeConfig(@Param("robotId") Long robotId, @Param("modeId") Long modeId);

    /**
     * 统计各模式机器人数量
     *
     * @return 统计结果
     */
    @MapKey("currentMode")
    public List<Map<String, Object>> selectRobotModeStats();

    /**
     * 查询最近模式切换的机器人
     *
     * @param limit 限制数量
     * @return 机器人列表
     */
    public List<SysRobot> selectRecentModeSwitchRobots(@Param("limit") Integer limit);

    /**
     * 更新最后模式切换时间
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int updateLastModeSwitchTime(Long robotId);

    /**
     * 增加模式切换次数
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int incrementModeSwitchCount(Long robotId);
}