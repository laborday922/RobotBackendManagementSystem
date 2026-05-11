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

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

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
            SysModeParam param = new SysModeParam();
            param.setModeId(modeId);
            Long tenantId = TenantContext.get();
            if(!isAdmin(tenantId))
                param.setTenantId(tenantId);
            List<SysModeParam> params = sysModeParamMapper.selectSysModeParamList(param);
            sysMode.setModeParams(params);
        }
        return sysMode;
    }

    /**
     * 查询模式列表 - 自动获取真实统计数据
     *
     * @param sysMode 模式
     * @return 模式集合
     */
    @Override
    public List<SysMode> selectSysModeList(SysMode sysMode)
    {
        // 设置租户过滤
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            sysMode.setTenantId(tenantId);

        // 先更新所有模式的统计数据，确保显示正确
        updateAllStatistics();

        List<SysMode> list = sysModeMapper.selectSysModeList(sysMode);
        // 为每个模式查询参数列表
        for (SysMode mode : list)
        {
            SysModeParam param = new SysModeParam();
            param.setModeId(mode.getModeId());
            if(!isAdmin(tenantId))
                param.setTenantId(tenantId);
            List<SysModeParam> params = sysModeParamMapper.selectSysModeParamList(param);
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
        // 设置租户ID
        sysMode.setTenantId(TenantContext.get());

        // 保存模式基本信息
        int result = sysModeMapper.insertSysMode(sysMode);

        // 保存参数列表
        if (sysMode.getModeParams() != null && !sysMode.getModeParams().isEmpty())
        {
            for (SysModeParam param : sysMode.getModeParams())
            {
                param.setModeId(sysMode.getModeId());
                param.setCreateBy(sysMode.getCreateBy());
                param.setTenantId(sysMode.getTenantId());  // 设置租户ID
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

        // 先删除原有参数（逻辑删除）
        sysModeParamMapper.deleteSysModeParamByModeId(sysMode.getModeId());

        // 保存新参数列表
        if (sysMode.getModeParams() != null && !sysMode.getModeParams().isEmpty())
        {
            Long tenantId = TenantContext.get();
            for (SysModeParam param : sysMode.getModeParams())
            {
                param.setModeId(sysMode.getModeId());
                param.setCreateBy(sysMode.getUpdateBy());
                param.setTenantId(tenantId);  // 设置租户ID
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

    /**
     * 增加模式使用次数
     * 在模式被使用时调用，插入一条历史记录
     *
     * @param modeId 模式ID
     * @return 结果
     */
    @Override
    public int incrementUsageCount(Long modeId) {
        if (modeId == null) {
            return 0;
        }
        try {
            return sysModeMapper.incrementUsageCount(modeId);
        } catch (Exception e) {
            logger.error("增加模式使用次数失败: modeId={}", modeId, e);
            return 0;
        }
    }

    /**
     * 更新指定模式的机器人数量
     *
     * @param modeId 模式ID
     */
    @Override
    public void updateRobotCountByModeId(Long modeId) {
        if (modeId == null) return;
        try {
            int result = sysModeMapper.updateRobotCount(modeId);
            if (result > 0) {
                logger.debug("模式机器人数量更新成功: modeId={}", modeId);
            }
        } catch (Exception e) {
            logger.error("更新模式机器人数量失败: modeId={}", modeId, e);
        }
    }

    /**
     * 更新所有模式的统计数据（机器人数量和使用次数）
     */
    @Override
    public void updateAllStatistics() {
        try {
            // 获取当前租户ID
            Long tenantId = TenantContext.get();
            if(isAdmin(tenantId)) {
                tenantId = null;  // 管理员更新所有租户
            }
            // 更新所有模式的机器人数量
            int robotCountResult = sysModeMapper.updateAllRobotCounts(tenantId);
            // 更新所有模式的使用次数
            int usageCountResult = sysModeMapper.updateAllUsageCounts(tenantId);
            logger.info("所有模式统计数据更新完成，机器人数量更新: {} 条，使用次数更新: {} 条",
                    robotCountResult, usageCountResult);
        } catch (Exception e) {
            logger.error("更新所有模式统计数据失败", e);
        }
    }
}