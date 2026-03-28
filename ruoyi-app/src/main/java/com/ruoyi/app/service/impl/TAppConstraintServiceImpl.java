package com.ruoyi.app.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.app.mapper.TAppConstraintMapper;
import com.ruoyi.app.domain.TAppConstraint;
import com.ruoyi.app.service.ITAppConstraintService;

/**
 * 应用级约束规则Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@Service
public class TAppConstraintServiceImpl implements ITAppConstraintService 
{
    @Autowired
    private TAppConstraintMapper tAppConstraintMapper;

    /**
     * 查询应用级约束规则
     * 
     * @param id 应用级约束规则主键
     * @return 应用级约束规则
     */
    @Override
    public TAppConstraint selectTAppConstraintById(Long id)
    {
        return tAppConstraintMapper.selectTAppConstraintById(id);
    }

    /**
     * 查询应用级约束规则列表
     * 
     * @param tAppConstraint 应用级约束规则
     * @return 应用级约束规则
     */
    @Override
    public List<TAppConstraint> selectTAppConstraintList(TAppConstraint tAppConstraint)
    {
        return tAppConstraintMapper.selectTAppConstraintList(tAppConstraint);
    }

    /**
     * 新增应用级约束规则
     * 
     * @param tAppConstraint 应用级约束规则
     * @return 结果
     */
    @Override
    public int insertTAppConstraint(TAppConstraint tAppConstraint)
    {
        return tAppConstraintMapper.insertTAppConstraint(tAppConstraint);
    }

    /**
     * 修改应用级约束规则
     * 
     * @param tAppConstraint 应用级约束规则
     * @return 结果
     */
    @Override
    public int updateTAppConstraint(TAppConstraint tAppConstraint)
    {
        return tAppConstraintMapper.updateTAppConstraint(tAppConstraint);
    }

    /**
     * 批量删除应用级约束规则
     * 
     * @param ids 需要删除的应用级约束规则主键
     * @return 结果
     */
    @Override
    public int deleteTAppConstraintByIds(Long[] ids)
    {
        return tAppConstraintMapper.deleteTAppConstraintByIds(ids);
    }

    /**
     * 删除应用级约束规则信息
     * 
     * @param id 应用级约束规则主键
     * @return 结果
     */
    @Override
    public int deleteTAppConstraintById(Long id)
    {
        return tAppConstraintMapper.deleteTAppConstraintById(id);
    }
}
