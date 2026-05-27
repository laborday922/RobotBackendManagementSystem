package com.ruoyi.robots.mapper;

import java.util.List;
import java.util.Date;
import com.ruoyi.robots.domain.RobotWarnings;
import org.apache.ibatis.annotations.Param;

/**
 * 机器人状态预警Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
public interface RobotWarningsMapper 
{
    /**
     * 查询机器人状态预警
     * 
     * @param id 机器人状态预警主键
     * @return 机器人状态预警
     */
    public RobotWarnings selectRobotWarningsById(Long id);

    /**
     * 查询机器人状态预警列表
     * 
     * @param robotWarnings 机器人状态预警
     * @return 机器人状态预警集合
     */
    public List<RobotWarnings> selectRobotWarningsList(RobotWarnings robotWarnings);

    /**
     * 新增机器人状态预警
     * 
     * @param robotWarnings 机器人状态预警
     * @return 结果
     */
    public int insertRobotWarnings(RobotWarnings robotWarnings);

    /**
     * 修改机器人状态预警
     * 
     * @param robotWarnings 机器人状态预警
     * @return 结果
     */
    public int updateRobotWarnings(RobotWarnings robotWarnings);

    int dealRobotWarningsByIds(@Param("ids") Long[] ids,
                              @Param("resolveTime") Date resolveTime,
                              @Param("resolveUser") String resolveUser,
                              @Param("resolveNote") String resolveNote);

    /**
     * 删除机器人状态预警
     * 
     * @param id 机器人状态预警主键
     * @return 结果
     */
    public int deleteRobotWarningsById(String id);

    /**
     * 批量删除机器人状态预警
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRobotWarningsByIds(Long[] ids);


}
