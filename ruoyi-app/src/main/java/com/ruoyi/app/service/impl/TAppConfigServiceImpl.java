package com.ruoyi.app.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.robots.exception.InsertNoAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.app.mapper.TAppConfigMapper;
import com.ruoyi.app.domain.TAppConfig;
import com.ruoyi.app.service.ITAppConfigService;

import static com.ruoyi.robots.common.RobotsConstants.ROBOT_CODE_HAS_EXISTED;

/**
 * 应用配置Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@Service
public class TAppConfigServiceImpl implements ITAppConfigService 
{
    @Autowired
    private TAppConfigMapper tAppConfigMapper;

    /**
     * 查询应用配置
     * 
     * @param id 应用配置主键
     * @return 应用配置
     */
    @Override
    public TAppConfig selectTAppConfigById(Long id)
    {
        return tAppConfigMapper.selectTAppConfigById(id);
    }

    /**
     * 查询应用配置列表
     * 
     * @param tAppConfig 应用配置
     * @return 应用配置
     */
    @Override
    public List<TAppConfig> selectTAppConfigList(TAppConfig tAppConfig)
    {
        return tAppConfigMapper.selectTAppConfigList(tAppConfig);
    }

    /**
     * 新增应用配置
     * 
     * @param tAppConfig 应用配置
     * @return 结果
     */
    @Override
    public int insertTAppConfig(TAppConfig tAppConfig)
    {
        int count = tAppConfigMapper.selectRobotGroupsByConfigKey(tAppConfig.getConfigKey(), tAppConfig.getAppId());
        if(count>0)throw new InsertNoAllowedException("配置项名已存在");
        tAppConfig.setCreateTime(DateUtils.getNowDate());
        return tAppConfigMapper.insertTAppConfig(tAppConfig);
    }

    /**
     * 修改应用配置
     * 
     * @param tAppConfig 应用配置
     * @return 结果
     */
    @Override
    public int updateTAppConfig(TAppConfig tAppConfig)
    {
        tAppConfig.setUpdateTime(DateUtils.getNowDate());
        return tAppConfigMapper.updateTAppConfig(tAppConfig);
    }

    /**
     * 批量删除应用配置
     * 
     * @param ids 需要删除的应用配置主键
     * @return 结果
     */
    @Override
    public int deleteTAppConfigByIds(Long[] ids)
    {
        return tAppConfigMapper.deleteTAppConfigByIds(ids);
    }

    /**
     * 删除应用配置信息
     * 
     * @param id 应用配置主键
     * @return 结果
     */
    @Override
    public int deleteTAppConfigById(Long id)
    {
        return tAppConfigMapper.deleteTAppConfigById(id);
    }
}
