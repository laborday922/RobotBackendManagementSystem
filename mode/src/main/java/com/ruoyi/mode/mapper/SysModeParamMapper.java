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

    public SysModeParam selectSysModeParamById(Long paramId);


    public List<SysModeParam> selectSysModeParamByModeId(Long modeId);


    public List<SysModeParam> selectSysModeParamList(SysModeParam sysModeParam);


    public int insertSysModeParam(SysModeParam sysModeParam);


    public int updateSysModeParam(SysModeParam sysModeParam);


    public int deleteSysModeParamById(Long paramId);


    public int deleteSysModeParamByIds(Long[] paramIds);


    public int deleteSysModeParamByModeId(Long modeId);
}