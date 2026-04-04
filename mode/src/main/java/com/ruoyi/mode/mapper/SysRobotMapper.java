package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysRobot;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 机器人Mapper接口
 *
 * @author ruoyi
 */
public interface SysRobotMapper
{
    /**
     * 查询机器人
     *
     * @param robotId 机器人ID
     * @return 机器人
     */
    public SysRobot selectSysRobotById(Long robotId);

    /**
     * 查询机器人列表
     *
     * @param sysRobot 机器人
     * @return 机器人集合
     */
    public List<SysRobot> selectSysRobotList(SysRobot sysRobot);

    /**
     * 查询在线机器人列表
     *
     * @return 在线机器人集合
     */
    public List<SysRobot> selectOnlineRobots();

    /**
     * 查询低电量机器人列表
     *
     * @param threshold 电量阈值
     * @return 机器人集合
     */
    public List<SysRobot> selectLowBatteryRobots(@Param("threshold") Integer threshold);

    /**
     * 根据分组ID查询机器人列表
     *
     * @param groupId 分组ID
     * @return 机器人集合
     */
    public List<SysRobot> selectRobotsByGroupId(Long groupId);

    /**
     * 根据状态查询机器人数量
     *
     * @param status 状态
     * @return 数量
     */
    public int selectRobotCountByStatus(String status);

    /**
     * 新增机器人
     *
     * @param sysRobot 机器人
     * @return 结果
     */
    public int insertSysRobot(SysRobot sysRobot);

    /**
     * 修改机器人
     *
     * @param sysRobot 机器人
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
     * 批量更新机器人状态
     *
     * @param robotIds 机器人ID数组
     * @param status 状态
     * @return 结果
     */
    public int batchUpdateRobotStatus(@Param("robotIds") Long[] robotIds, @Param("status") String status);

    /**
     * 更新机器人电量
     *
     * @param robotId 机器人ID
     * @param battery 电量
     * @return 结果
     */
    public int updateRobotBattery(@Param("robotId") Long robotId, @Param("battery") Long battery);

    /**
     * 更新机器人位置
     *
     * @param robotId 机器人ID
     * @param location 位置
     * @return 结果
     */
    public int updateRobotLocation(@Param("robotId") Long robotId, @Param("location") String location);

    /**
     * 增加完成任务数
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int incrementTasksCompleted(Long robotId);

    /**
     * 删除机器人（逻辑删除）
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int deleteSysRobotById(Long robotId);

    /**
     * 批量删除机器人（逻辑删除）
     *
     * @param robotIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysRobotByIds(Long[] robotIds);

    /**
     * 物理删除机器人（慎用）
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int deleteSysRobotPhysically(Long robotId);

    /**
     * 批量物理删除机器人（慎用）
     *
     * @param robotIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysRobotPhysicallyByIds(Long[] robotIds);

    /**
     * 检查机器人编码是否唯一
     *
     * @param robotCode 机器人编码
     * @return 结果
     */
    public int checkRobotCodeUnique(String robotCode);

    /**
     * 检查机器人名称是否唯一
     *
     * @param robotName 机器人名称
     * @return 结果
     */
    public int checkRobotNameUnique(String robotName);

    /**
     * 统计各状态机器人数量
     *
     * @return 统计结果
     */
    @MapKey("status")
    public List<Map<String, Object>> selectRobotStatusStats();

    /**
     * 统计各组机器人数量
     *
     * @return 统计结果
     */
    @MapKey("status")
    public List<Map<String, Object>> selectRobotGroupStats();

    /**
     * 查询需要充电的机器人
     *
     * @param threshold 电量阈值
     * @return 机器人集合
     */
    public List<SysRobot> selectRobotsNeedCharge(@Param("threshold") Integer threshold);

    /**
     * 查询最近活跃的机器人
     *
     * @param minutes 最近几分钟
     * @return 机器人集合
     */
    public List<SysRobot> selectRecentActiveRobots(@Param("minutes") Integer minutes);
}