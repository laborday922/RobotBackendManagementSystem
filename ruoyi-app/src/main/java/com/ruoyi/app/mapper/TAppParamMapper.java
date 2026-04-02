package com.ruoyi.app.mapper;

import java.util.List;
import com.ruoyi.app.domain.TAppParam;

/**
 * 应用能力参数定义Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
public interface TAppParamMapper 
{
    /**
     * 查询应用能力参数定义
     * 
     * @param id 应用能力参数定义主键
     * @return 应用能力参数定义
     */
    public TAppParam selectTAppParamById(Long id);

    /**
     * 查询应用能力参数定义列表
     * 
     * @param tAppParam 应用能力参数定义
     * @return 应用能力参数定义集合
     */
    public List<TAppParam> selectTAppParamList(TAppParam tAppParam);

    /**
     * 新增应用能力参数定义
     * 
     * @param tAppParam 应用能力参数定义
     * @return 结果
     */
    public int insertTAppParam(TAppParam tAppParam);

    /**
     * 修改应用能力参数定义
     * 
     * @param tAppParam 应用能力参数定义
     * @return 结果
     */
    public int updateTAppParam(TAppParam tAppParam);

    /**
     * 删除应用能力参数定义
     * 
     * @param id 应用能力参数定义主键
     * @return 结果
     */
    public int deleteTAppParamById(Long id);

    /**
     * 批量删除应用能力参数定义
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTAppParamByIds(Long[] ids);
}
