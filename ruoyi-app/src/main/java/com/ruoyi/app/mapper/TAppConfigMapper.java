package com.ruoyi.app.mapper;

import java.util.List;
import com.ruoyi.app.domain.TAppConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 应用配置Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
public interface TAppConfigMapper 
{
    /**
     * 查询应用配置
     * 
     * @param id 应用配置主键
     * @return 应用配置
     */
    public TAppConfig selectTAppConfigById(Long id);

    /**
     * 查询应用配置列表
     * 
     * @param tAppConfig 应用配置
     * @return 应用配置集合
     */
    public List<TAppConfig> selectTAppConfigList(TAppConfig tAppConfig);

    /**
     * 新增应用配置
     * 
     * @param tAppConfig 应用配置
     * @return 结果
     */
    public int insertTAppConfig(TAppConfig tAppConfig);

    /**
     * 修改应用配置
     * 
     * @param tAppConfig 应用配置
     * @return 结果
     */
    public int updateTAppConfig(TAppConfig tAppConfig);

    /**
     * 删除应用配置
     * 
     * @param id 应用配置主键
     * @return 结果
     */
    public int deleteTAppConfigById(Long id);

    /**
     * 批量删除应用配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTAppConfigByIds(Long[] ids);

    @Select("select count(id) from t_app_config where config_key = #{configKey} and app_id=#{appId}")
    int selectRobotGroupsByConfigKey(@Param("configKey")String configKey, @Param("appId")String appId);
}
