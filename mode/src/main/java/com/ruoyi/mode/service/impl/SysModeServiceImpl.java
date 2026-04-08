package com.ruoyi.mode.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.domain.SysModeParam;
import com.ruoyi.mode.mapper.SysModeMapper;
import com.ruoyi.mode.mapper.SysModeParamMapper;
import com.ruoyi.mode.service.ISysModeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(SysModeServiceImpl.class);

    @Autowired
    private SysModeMapper sysModeMapper;

    @Autowired
    private SysModeParamMapper sysModeParamMapper;

    /**
     * 获取当前租户ID
     */
    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    /**
     * 查询模式
     *
     * @param modeId 模式ID
     * @return 模式
     */
    @Override
    public SysMode selectSysModeById(Long modeId)
    {
        SysMode query = new SysMode();
        query.setModeId(modeId);
        query.setTenantId(getCurrentTenantId());

        SysMode sysMode = sysModeMapper.selectSysModeById(query);
        if (sysMode != null)
        {
            SysModeParam paramQuery = new SysModeParam();
            paramQuery.setModeId(modeId);
            paramQuery.setTenantId(getCurrentTenantId());
            List<SysModeParam> params = sysModeParamMapper.selectSysModeParamList(paramQuery);
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
        if (sysMode == null) {
            sysMode = new SysMode();
        }
        sysMode.setTenantId(getCurrentTenantId());

        List<SysMode> list = sysModeMapper.selectSysModeList(sysMode);
        for (SysMode mode : list)
        {
            SysModeParam paramQuery = new SysModeParam();
            paramQuery.setModeId(mode.getModeId());
            paramQuery.setTenantId(getCurrentTenantId());
            List<SysModeParam> params = sysModeParamMapper.selectSysModeParamList(paramQuery);
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
        sysMode.setTenantId(getCurrentTenantId());
        sysMode.setCreateTime(DateUtils.getNowDate());

        int result = sysModeMapper.insertSysMode(sysMode);

        if (sysMode.getModeParams() != null && !sysMode.getModeParams().isEmpty())
        {
            for (SysModeParam param : sysMode.getModeParams())
            {
                param.setModeId(sysMode.getModeId());
                param.setCreateBy(sysMode.getCreateBy());
                param.setTenantId(sysMode.getTenantId());
                param.setCreateTime(DateUtils.getNowDate());
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
        sysMode.setUpdateTime(DateUtils.getNowDate());

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
                param.setTenantId(sysMode.getTenantId());
                param.setCreateTime(DateUtils.getNowDate());
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
        // 验证这些模式是否都属于当前租户
        Long tenantId = getCurrentTenantId();
        for (Long modeId : modeIds) {
            SysMode query = new SysMode();
            query.setModeId(modeId);
            query.setTenantId(tenantId);
            SysMode mode = sysModeMapper.selectSysModeById(query);
            if (mode == null) {
                logger.warn("模式不存在或不属于当前租户: modeId={}, tenantId={}", modeId, tenantId);
                throw new RuntimeException("无权操作其他租户的数据");
            }
        }

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
        // 验证模式是否属于当前租户
        Long tenantId = getCurrentTenantId();
        SysMode query = new SysMode();
        query.setModeId(modeId);
        query.setTenantId(tenantId);
        SysMode mode = sysModeMapper.selectSysModeById(query);
        if (mode == null) {
            logger.warn("模式不存在或不属于当前租户: modeId={}, tenantId={}", modeId, tenantId);
            throw new RuntimeException("无权操作其他租户的数据");
        }

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
        // 验证模式是否属于当前租户
        Long tenantId = getCurrentTenantId();
        SysMode query = new SysMode();
        query.setModeId(modeId);
        query.setTenantId(tenantId);
        SysMode mode = sysModeMapper.selectSysModeById(query);
        if (mode == null) {
            logger.warn("模式不存在或不属于当前租户: modeId={}, tenantId={}", modeId, tenantId);
            throw new RuntimeException("无权操作其他租户的数据");
        }

        SysMode sysMode = new SysMode();
        sysMode.setModeId(modeId);
        sysMode.setEnabled(enabled);
        sysMode.setUpdateTime(DateUtils.getNowDate());
        return sysModeMapper.updateSysMode(sysMode);
    }
}