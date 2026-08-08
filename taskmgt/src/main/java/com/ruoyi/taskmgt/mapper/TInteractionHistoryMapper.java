package com.ruoyi.taskmgt.mapper;

import java.util.List;
import com.ruoyi.taskmgt.domain.TInteractionHistory;
import com.ruoyi.taskmgt.domain.vo.SumOfInteractionHistoryVo;
import org.apache.ibatis.annotations.Select;

/**
 * 交互历史记录Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
public interface TInteractionHistoryMapper 
{
    /**
     * 查询交互历史记录
     * 
     * @param id 交互历史记录主键
     * @return 交互历史记录
     */
    public TInteractionHistory selectTInteractionHistoryById(Long id);

    /**
     * 查询交互历史记录列表
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 交互历史记录集合
     */
    public List<TInteractionHistory> selectTInteractionHistoryList(TInteractionHistory tInteractionHistory);

    /**
     * 新增交互历史记录
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    public int insertTInteractionHistory(TInteractionHistory tInteractionHistory);

    /**
     * 修改交互历史记录
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    public int updateTInteractionHistory(TInteractionHistory tInteractionHistory);

    /**
     * 删除交互历史记录
     * 
     * @param id 交互历史记录主键
     * @return 结果
     */
    public int deleteTInteractionHistoryById(Long id);

    /**
     * 批量删除交互历史记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTInteractionHistoryByIds(Long[] ids);

    @Select("SELECT \n" +
            "    COUNT(*) AS total_times," +
            "    SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) AS good_times," +
            "    SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS mid_times," +
            "    SUM(CASE WHEN rating = 0 THEN 1 ELSE 0 END) AS bad_times " +
            "FROM t_interaction_history;")
    SumOfInteractionHistoryVo sumOfInteractionHistory();
}
