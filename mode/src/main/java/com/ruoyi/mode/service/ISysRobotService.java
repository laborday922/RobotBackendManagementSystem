package com.ruoyi.mode.service;

import java.util.List;
import java.util.Map;

/**
 * 机器人模式管理Service接口
 * sys_robot_ext 表已废弃，current_mode 直接存储在 robots 表中
 *
 * @author ruoyi
 */
public interface ISysRobotService
{
    /**
     * 更新机器人当前模式（直接操作 robots 表）
     *
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return 结果
     */
    public int updateRobotMode(Long robotId, Long modeId);

    /**
     * 批量重启机器人（同步）
     */
    public int batchRestart(Long[] robotIds);

    /**
     * 批量重启机器人（异步，立即返回）
     */
    public int batchRestartAsync(Long[] robotIds);

    /**
     * 紧急停止机器人
     */
    public int emergencyStop(Long[] robotIds);

    /**
     * 紧急撤离
     */
    public int emergencyEvacuation(Long[] robotIds);

    /**
     * 刷新机器人状态
     */
    public int refreshStatus(Long[] robotIds);

    /**
     * 测试告警
     */
    public int testAlert(Long[] robotIds);

    /**
     * 清除告警
     */
    public int clearAlerts(Long[] robotIds);

    // ==================== 模式切换操作接口 ====================

    /**
     * 切换待机模式
     */
    public int standbyMode(Long[] robotIds);

    /**
     * 切换维护模式（暂停全部任务）
     */
    public int maintenanceMode(Long[] robotIds);

    /**
     * 切换充电模式
     */
    public Map<String, Object> chargeMode(Long[] robotIds);

    /**
     * 返回充电
     */
    public int returnCharge(Long[] robotIds);

    // ==================== 机器人模式配置相关方法 ====================

    /**
     * 保存机器人模式配置
     */
    public int saveRobotModeConfig(Long robotId, Long modeId, Map<String, Object> config);

    /**
     * 获取机器人模式配置
     */
    public Map<String, Object> getRobotModeConfig(Long robotId, Long modeId);

    /**
     * 删除机器人模式配置
     */
    public int deleteRobotModeConfig(Long robotId, Long modeId);

    /**
     * 复制机器人模式配置
     */
    public int copyRobotModeConfig(Long sourceRobotId, Long targetRobotId, Long modeId);

    // ==================== WebSocket 模式切换方法 ====================

    /**
     * 通过 WebSocket 切换机器人模式（同步），携带 sys_mode_param 参数
     */
    public boolean switchModeViaWebSocketSync(Long robotId, Long modeId, String modeName);
}
