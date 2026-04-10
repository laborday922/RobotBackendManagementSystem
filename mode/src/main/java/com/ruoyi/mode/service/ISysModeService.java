package com.ruoyi.mode.service;

import com.ruoyi.mode.domain.SysMode;

import java.util.List;

/**
 * 模式Service接口
 *
 * @author ruoyi
 */
public interface ISysModeService
{
    /**
     * 查询模式
     *
     * @param modeId 模式ID
     * @return 模式
     */
    public SysMode selectSysModeById(Long modeId);

    /**
     * 查询模式列表
     *
     * @param sysMode 模式
     * @return 模式集合
     */
    public List<SysMode> selectSysModeList(SysMode sysMode);

    /**
     * 新增模式
     *
     * @param sysMode 模式
     * @return 结果
     */
    public int insertSysMode(SysMode sysMode);

    /**
     * 修改模式
     *
     * @param sysMode 模式
     * @return 结果
     */
    public int updateSysMode(SysMode sysMode);

    /**
     * 批量删除模式
     *
     * @param modeIds 需要删除的模式ID
     * @return 结果
     */
    public int deleteSysModeByIds(Long[] modeIds);

    /**
     * 删除模式信息
     *
     * @param modeId 模式ID
     * @return 结果
     */
    public int deleteSysModeById(Long modeId);

    /**
     * 启用/禁用模式
     *
     * @param modeId 模式ID
     * @param enabled 启用状态
     * @return 结果
     */
    public int changeModeStatus(Long modeId, String enabled);

    /**
     * 增加模式使用次数
     *
     * @param modeId 模式ID
     * @return 结果
     */
    public int incrementUsageCount(Long modeId);

    /**
     * 更新指定模式的机器人数量
     *
     * @param modeId 模式ID
     */
    public void updateRobotCountByModeId(Long modeId);

    /**
     * 更新所有模式的统计数据（机器人数量和使用次数）
     */
    public void updateAllStatistics();
}