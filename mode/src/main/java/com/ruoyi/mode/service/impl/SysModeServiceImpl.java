package com.ruoyi.mode.service.impl;

import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.domain.SysModeParam;
import com.ruoyi.mode.mapper.SysModeMapper;
import com.ruoyi.mode.mapper.SysModeParamMapper;
import com.ruoyi.mode.service.ISysModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模式Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysModeServiceImpl implements ISysModeService
{
    @Autowired
    private SysModeMapper sysModeMapper;

    @Autowired
    private SysModeParamMapper sysModeParamMapper;

    /**
     * 查询模式
     *
     * @param modeId 模式ID
     * @return 模式
     */
    @Override
    public SysMode selectSysModeById(Long modeId)
    {
        SysMode sysMode = sysModeMapper.selectSysModeById(modeId);
        if (sysMode != null)
        {
            // 查询关联的参数列表
            List<SysModeParam> params = sysModeParamMapper.selectSysModeParamByModeId(modeId);
            sysMode.setModeParams(params);
        }
        return sysMode;
    }

    /**
     * 查询模式列表
     *
     * @param sysMode 模式
     * @return 模式集合
     */
    @Override
    public List<SysMode> selectSysModeList(SysMode sysMode)
    {
        List<SysMode> list = sysModeMapper.selectSysModeList(sysMode);
        // 为每个模式查询参数数量
        for (SysMode mode : list)
        {
            List<SysModeParam> params = sysModeParamMapper.selectSysModeParamByModeId(mode.getModeId());
            if (params != null)
            {
                mode.setModeParams(params);
            }
        }
        return list;
    }

    /**
     * 新增模式
     *
     * @param sysMode 模式
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSysMode(SysMode sysMode)
    {
        // 保存模式基本信息
        int result = sysModeMapper.insertSysMode(sysMode);

        // 保存参数列表
        if (sysMode.getModeParams() != null && !sysMode.getModeParams().isEmpty())
        {
            for (SysModeParam param : sysMode.getModeParams())
            {
                param.setModeId(sysMode.getModeId());
                param.setCreateBy(sysMode.getCreateBy());
                sysModeParamMapper.insertSysModeParam(param);
            }
        }

        return result;
    }

    /**
     * 修改模式
     *
     * @param sysMode 模式
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSysMode(SysMode sysMode)
    {
        // 更新模式基本信息
        int result = sysModeMapper.updateSysMode(sysMode);

        // 先删除原有参数
        sysModeParamMapper.deleteSysModeParamByModeId(sysMode.getModeId());

        // 保存新参数列表
        if (sysMode.getModeParams() != null && !sysMode.getModeParams().isEmpty())
        {
            for (SysModeParam param : sysMode.getModeParams())
            {
                param.setModeId(sysMode.getModeId());
                param.setCreateBy(sysMode.getUpdateBy());
                sysModeParamMapper.insertSysModeParam(param);
            }
        }

        return result;
    }

    /**
     * 批量删除模式
     *
     * @param modeIds 需要删除的模式ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysModeByIds(Long[] modeIds)
    {
        // 删除模式关联的参数
        for (Long modeId : modeIds)
        {
            sysModeParamMapper.deleteSysModeParamByModeId(modeId);
        }
        // 删除模式
        return sysModeMapper.deleteSysModeByIds(modeIds);
    }

    /**
     * 删除模式信息
     *
     * @param modeId 模式ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysModeById(Long modeId)
    {
        // 删除模式关联的参数
        sysModeParamMapper.deleteSysModeParamByModeId(modeId);
        // 删除模式
        return sysModeMapper.deleteSysModeById(modeId);
    }

    /**
     * 启用/禁用模式
     *
     * @param modeId 模式ID
     * @param enabled 启用状态
     * @return 结果
     */
    @Override
    public int changeModeStatus(Long modeId, String enabled)
    {
        SysMode sysMode = new SysMode();
        sysMode.setModeId(modeId);
        sysMode.setEnabled(enabled);
        return sysModeMapper.updateSysMode(sysMode);
    }
}