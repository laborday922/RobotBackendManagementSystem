package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysMode;

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
}