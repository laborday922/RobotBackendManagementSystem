package com.ruoyi.app.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.app.mapper.TAppParamMapper;
import com.ruoyi.app.domain.TAppParam;
import com.ruoyi.app.service.ITAppParamService;

/**
 * 应用能力参数定义Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@Service
public class TAppParamServiceImpl implements ITAppParamService 
{
    @Autowired
    private TAppParamMapper tAppParamMapper;

    /**
     * 查询应用能力参数定义
     * 
     * @param id 应用能力参数定义主键
     * @return 应用能力参数定义
     */
    @Override
    public TAppParam selectTAppParamById(Long id)
    {
        return tAppParamMapper.selectTAppParamById(id);
    }

    /**
     * 查询应用能力参数定义列表
     * 
     * @param tAppParam 应用能力参数定义
     * @return 应用能力参数定义
     */
    @Override
    public List<TAppParam> selectTAppParamList(TAppParam tAppParam)
    {
        return tAppParamMapper.selectTAppParamList(tAppParam);
    }

    /**
     * 新增应用能力参数定义
     * 
     * @param tAppParam 应用能力参数定义
     * @return 结果
     */
    @Override
    public int insertTAppParam(TAppParam tAppParam)
    {
        return tAppParamMapper.insertTAppParam(tAppParam);
    }

    /**
     * 修改应用能力参数定义
     * 
     * @param tAppParam 应用能力参数定义
     * @return 结果
     */
    @Override
    public int updateTAppParam(TAppParam tAppParam)
    {
        return tAppParamMapper.updateTAppParam(tAppParam);
    }

    /**
     * 批量删除应用能力参数定义
     * 
     * @param ids 需要删除的应用能力参数定义主键
     * @return 结果
     */
    @Override
    public int deleteTAppParamByIds(Long[] ids)
    {
        return tAppParamMapper.deleteTAppParamByIds(ids);
    }

    /**
     * 删除应用能力参数定义信息
     * 
     * @param id 应用能力参数定义主键
     * @return 结果
     */
    @Override
    public int deleteTAppParamById(Long id)
    {
        return tAppParamMapper.deleteTAppParamById(id);
    }
}
