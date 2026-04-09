package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysModeParam;

import java.util.List;

/**
 * 模式参数Mapper接口
 *
 * @author ruoyi
 */
public interface SysModeParamMapper
{
    /**
     * 查询模式参数
     *
     * @param paramId 参数ID
     * @return 模式参数
     */
    public SysModeParam selectSysModeParamById(Long paramId);

    /**
     * 根据模式ID查询参数列表
     *
     * @param modeId 模式ID
     * @return 参数集合
     */
    public List<SysModeParam> selectSysModeParamByModeId(Long modeId);

    /**
     * 查询模式参数列表
     *
     * @param sysModeParam 模式参数
     * @return 模式参数集合
     */
    public List<SysModeParam> selectSysModeParamList(SysModeParam sysModeParam);

    /**
     * 新增模式参数
     *
     * @param sysModeParam 模式参数
     * @return 结果
     */
    public int insertSysModeParam(SysModeParam sysModeParam);

    /**
     * 修改模式参数
     *
     * @param sysModeParam 模式参数
     * @return 结果
     */
    public int updateSysModeParam(SysModeParam sysModeParam);

    /**
     * 删除模式参数
     *
     * @param paramId 参数ID
     * @return 结果
     */
    public int deleteSysModeParamById(Long paramId);

    /**
     * 批量删除模式参数
     *
     * @param paramIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysModeParamByIds(Long[] paramIds);

    /**
     * 根据模式ID删除参数
     *
     * @param modeId 模式ID
     * @return 结果
     */
    public int deleteSysModeParamByModeId(Long modeId);
}