package com.ruoyi.robots.mapper;

import java.util.List;
import com.ruoyi.robots.domain.RobotGroups;
import org.apache.ibatis.annotations.Select;

/**
 * 机器人分组Mapper接口
 *
 * @author xiaocai
 * @date 2026-03-07
 */
public interface RobotGroupsMapper
{
    /**
     * 查询机器人分组
     *
     * @param id 机器人分组主键
     * @return 机器人分组
     */
    public RobotGroups selectRobotGroupsById(Long id);

    /**
     * 查询机器人分组列表
     *
     * @param robotGroups 机器人分组
     * @return 机器人分组集合
     */
    public List<RobotGroups> selectRobotGroupsList(RobotGroups robotGroups);

    /**
     * 新增机器人分组
     *
     * @param robotGroups 机器人分组
     * @return 结果
     */
    public int insertRobotGroups(RobotGroups robotGroups);

    /**
     * 修改机器人分组
     *
     * @param robotGroups 机器人分组
     * @return 结果
     */
    public int updateRobotGroups(RobotGroups robotGroups);

    /**
     * 删除机器人分组
     *
     * @param id 机器人分组主键
     * @return 结果
     */
    public int deleteRobotGroupsById(Long id);

    /**
     * 批量删除机器人分组
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRobotGroupsByIds(Long[] ids);

    @Select("select count(id) from robot_groups where name = #{name}")
    public Integer selectRobotGroupsByName(String name);
}
