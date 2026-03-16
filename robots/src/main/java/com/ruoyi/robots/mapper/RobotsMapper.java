package com.ruoyi.robots.mapper;

import java.util.List;

import com.ruoyi.robots.domain.Robot;
import org.apache.ibatis.annotations.Select;

/**
 * 机器人基础信息Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
public interface RobotsMapper 
{
    /**
     * 查询机器人基础信息
     * 
     * @param id 机器人基础信息主键
     * @return 机器人基础信息
     */
    public Robot selectRobotsById(Long id);

    /**
     * 查询机器人基础信息列表
     * 
     * @param robot 机器人基础信息
     * @return 机器人基础信息集合
     */
    public List<Robot> selectRobotsList(Robot robot);

    /**
     * 新增机器人基础信息
     * 
     * @param robot 机器人基础信息
     * @return 结果
     */
    public int insertRobots(Robot robot);

    /**
     * 修改机器人基础信息
     * 
     * @param robot 机器人基础信息
     * @return 结果
     */
    public int updateRobots(Robot robot);

    /**
     * 删除机器人基础信息
     * 
     * @param id 机器人基础信息主键
     * @return 结果
     */
    public int deleteRobotsById(String id);

    /**
     * 批量删除机器人基础信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRobotsByIds(String[] ids);

    @Select("select count(id) from robots where group_id = #{groupId}")
    public Integer countByGroupId(Long categoryId);

    @Select("select count(id) from robots where group_id = #{groupId}")
    int selectRobotsByCode(String code);
}
