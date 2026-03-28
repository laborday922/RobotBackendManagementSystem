package com.ruoyi.app.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.app.mapper.TAppApiMapper;
import com.ruoyi.app.domain.TAppApi;
import com.ruoyi.app.service.ITAppApiService;

/**
 * 支持的APIService业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@Service
public class TAppApiServiceImpl implements ITAppApiService 
{
    @Autowired
    private TAppApiMapper tAppApiMapper;

    /**
     * 查询支持的API
     * 
     * @param id 支持的API主键
     * @return 支持的API
     */
    @Override
    public TAppApi selectTAppApiById(Long id)
    {
        return tAppApiMapper.selectTAppApiById(id);
    }

    /**
     * 查询支持的API列表
     * 
     * @param tAppApi 支持的API
     * @return 支持的API
     */
    @Override
    public List<TAppApi> selectTAppApiList(TAppApi tAppApi)
    {
        return tAppApiMapper.selectTAppApiList(tAppApi);
    }

    /**
     * 新增支持的API
     * 
     * @param tAppApi 支持的API
     * @return 结果
     */
    @Override
    public int insertTAppApi(TAppApi tAppApi)
    {
        return tAppApiMapper.insertTAppApi(tAppApi);
    }

    /**
     * 修改支持的API
     * 
     * @param tAppApi 支持的API
     * @return 结果
     */
    @Override
    public int updateTAppApi(TAppApi tAppApi)
    {
        return tAppApiMapper.updateTAppApi(tAppApi);
    }

    /**
     * 批量删除支持的API
     * 
     * @param ids 需要删除的支持的API主键
     * @return 结果
     */
    @Override
    public int deleteTAppApiByIds(Long[] ids)
    {
        return tAppApiMapper.deleteTAppApiByIds(ids);
    }

    /**
     * 删除支持的API信息
     * 
     * @param id 支持的API主键
     * @return 结果
     */
    @Override
    public int deleteTAppApiById(Long id)
    {
        return tAppApiMapper.deleteTAppApiById(id);
    }
}
