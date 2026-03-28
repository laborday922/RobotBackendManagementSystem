package com.ruoyi.app.mapper;

import java.util.List;
import com.ruoyi.app.domain.TAppConstraint;

/**
 * 应用级约束规则Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
public interface TAppConstraintMapper 
{
    /**
     * 查询应用级约束规则
     * 
     * @param id 应用级约束规则主键
     * @return 应用级约束规则
     */
    public TAppConstraint selectTAppConstraintById(Long id);

    /**
     * 查询应用级约束规则列表
     * 
     * @param tAppConstraint 应用级约束规则
     * @return 应用级约束规则集合
     */
    public List<TAppConstraint> selectTAppConstraintList(TAppConstraint tAppConstraint);

    /**
     * 新增应用级约束规则
     * 
     * @param tAppConstraint 应用级约束规则
     * @return 结果
     */
    public int insertTAppConstraint(TAppConstraint tAppConstraint);

    /**
     * 修改应用级约束规则
     * 
     * @param tAppConstraint 应用级约束规则
     * @return 结果
     */
    public int updateTAppConstraint(TAppConstraint tAppConstraint);

    /**
     * 删除应用级约束规则
     * 
     * @param id 应用级约束规则主键
     * @return 结果
     */
    public int deleteTAppConstraintById(Long id);

    /**
     * 批量删除应用级约束规则
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTAppConstraintByIds(Long[] ids);
}
