package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysMode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模式Mapper接口
 *
 * @author ruoyi
 */
public interface SysModeMapper
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
     * 删除模式
     *
     * @param modeId 模式ID
     * @return 结果
     */
    public int deleteSysModeById(Long modeId);

    /**
     * 批量删除模式
     *
     * @param modeIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysModeByIds(Long[] modeIds);

    /**
     * 增加模式使用次数
     *
     * @param modeId 模式ID
     * @return 结果
     */
    public int incrementUsageCount(@Param("modeId") Long modeId);

    /**
     * 更新单个模式的机器人数量
     *
     * @param modeId 模式ID
     * @return 结果
     */
    public int updateRobotCount(Long modeId);

    /**
     * 更新所有模式的机器人数量
     *
     * @param tenantId 租户ID（为null时更新所有租户）
     * @return 结果
     */
    public int updateAllRobotCounts(@Param("tenantId") Long tenantId);

    /**
     * 更新所有模式的使用次数
     *
     * @param tenantId 租户ID（为null时更新所有租户）
     * @return 结果
     */
    public int updateAllUsageCounts(@Param("tenantId") Long tenantId);
}