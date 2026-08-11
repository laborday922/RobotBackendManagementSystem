package com.ruoyi.mode.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 机器人模式配置Mapper接口
 * sys_robot_ext 表已废弃，current_mode 直接存储在 robots 表中
 *
 * @author ruoyi
 */
public interface SysRobotMapper
{
    /**
     * 保存机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @param config 配置JSON
     * @param tenantId 租户ID
     * @return 结果
     */
    public int saveRobotModeConfig(@Param("robotId") Long robotId, @Param("modeId") Long modeId,
                                   @Param("config") String config, @Param("tenantId") Long tenantId);

    /**
     * 获取机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 配置JSON
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
}
