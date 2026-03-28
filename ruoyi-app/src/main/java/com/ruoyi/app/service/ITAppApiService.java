package com.ruoyi.app.service;

import java.util.List;
import com.ruoyi.app.domain.TAppApi;

/**
 * 支持的APIService接口
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
public interface ITAppApiService 
{
    /**
     * 查询支持的API
     * 
     * @param id 支持的API主键
     * @return 支持的API
     */
    public TAppApi selectTAppApiById(Long id);

    /**
     * 查询支持的API列表
     * 
     * @param tAppApi 支持的API
     * @return 支持的API集合
     */
    public List<TAppApi> selectTAppApiList(TAppApi tAppApi);

    /**
     * 新增支持的API
     * 
     * @param tAppApi 支持的API
     * @return 结果
     */
    public int insertTAppApi(TAppApi tAppApi);

    /**
     * 修改支持的API
     * 
     * @param tAppApi 支持的API
     * @return 结果
     */
    public int updateTAppApi(TAppApi tAppApi);

    /**
     * 批量删除支持的API
     * 
     * @param ids 需要删除的支持的API主键集合
     * @return 结果
     */
    public int deleteTAppApiByIds(Long[] ids);

    /**
     * 删除支持的API信息
     * 
     * @param id 支持的API主键
     * @return 结果
     */
    public int deleteTAppApiById(Long id);
}
