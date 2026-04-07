package com.ruoyi.mode.service;

import com.ruoyi.mode.domain.SysRobot;

import java.util.List;
import java.util.Map;

/**
 * 机器人Service接口
 *
 * @author ruoyi
 */
public interface ISysRobotService
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
     * 批量删除机器人
     *
     * @param robotIds 需要删除的机器人ID
     * @return 结果
     */
    public int deleteSysRobotByIds(Long[] robotIds);

    /**
     * 删除机器人信息
     *
     * @param robotId 机器人ID
     * @return 结果
     */
    public int deleteSysRobotById(Long robotId);

    /**
     * 更新机器人当前模式
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 结果
     */
    public int updateRobotMode(Long robotId, Long modeId);

    /**
     * 批量重启机器人（同步）
     *
     * @param robotIds 机器人ID数组
     * @return 成功重启的数量
     */
    public int batchRestart(Long[] robotIds);

    /**
     * 批量重启机器人（异步，立即返回）
     *
     * @param robotIds 机器人ID数组
     * @return 提交的任务数量
     */
    public int batchRestartAsync(Long[] robotIds);

    /**
     * 紧急停止机器人
     *
     * @param robotIds 机器人ID数组
     * @return 成功停止的数量
     */
    public int emergencyStop(Long[] robotIds);

    /**
     * 刷新机器人状态
     *
     * @param robotIds 机器人ID数组
     * @return 成功刷新的数量
     */
    public int refreshStatus(Long[] robotIds);

    /**
     * 测试告警
     *
     * @param robotIds 机器人ID数组
     * @return 成功触发告警的数量
     */
    public int testAlert(Long[] robotIds);

    /**
     * 清除告警
     *
     * @param robotIds 机器人ID数组
     * @return 成功清除告警的数量
     */
    public int clearAlerts(Long[] robotIds);

    /**
     * 获取在线机器人列表
     *
     * @return 在线机器人列表
     */
    public List<SysRobot> selectOnlineRobots();

    /**
     * 获取低电量机器人列表
     *
     * @param threshold 电量阈值
     * @return 低电量机器人列表
     */
    public List<SysRobot> selectLowBatteryRobots(Integer threshold);

    // ==================== 模式切换操作接口 ====================

    /**
     * 切换待机模式
     *
     * @param robotIds 机器人ID数组
     * @return 成功切换的数量
     */
    public int standbyMode(Long[] robotIds);

    /**
     * 切换维护模式
     *
     * @param robotIds 机器人ID数组
     * @return 成功切换的数量
     */
    public int maintenanceMode(Long[] robotIds);

    /**
     * 切换充电模式
     *
     * @param robotIds 机器人ID数组
     * @return 成功切换的数量
     */
    public int chargeMode(Long[] robotIds);

    /**
     * 返回充电
     *
     * @param robotIds 机器人ID数组
     * @return 成功操作的数量
     */
    public int returnCharge(Long[] robotIds);

    // ==================== 机器人模式配置相关方法 ====================

    /**
     * 保存机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @param config 配置参数
     * @return 结果
     */
    public int saveRobotModeConfig(Long robotId, Long modeId, Map<String, Object> config);

    /**
     * 获取机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 配置参数
     */
    public Map<String, Object> getRobotModeConfig(Long robotId, Long modeId);

    /**
     * 删除机器人模式配置
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 结果
     */
    public int deleteRobotModeConfig(Long robotId, Long modeId);

    /**
     * 复制机器人模式配置
     *
     * @param sourceRobotId 源机器人ID
     * @param targetRobotId 目标机器人ID
     * @param modeId 模式ID
     * @return 结果
     */
    public int copyRobotModeConfig(Long sourceRobotId, Long targetRobotId, Long modeId);
}