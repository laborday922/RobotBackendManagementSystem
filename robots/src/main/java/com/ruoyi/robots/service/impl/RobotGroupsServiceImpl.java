package com.ruoyi.robots.service.impl;

import java.util.Date;
import java.util.List;

import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.exception.DeleteNoAllowedException;
import com.ruoyi.robots.exception.InsertNoAllowedException;
import com.ruoyi.robots.mapper.RobotsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.robots.mapper.RobotGroupsMapper;
import com.ruoyi.robots.domain.RobotGroups;
import com.ruoyi.robots.service.IRobotGroupsService;

import static com.ruoyi.robots.common.RobotsConstants.*;

/**
 * 机器人分组Service业务层处理
 *
 * @author xiaocai
 * @date 2026-03-07
 */
@Service
public class RobotGroupsServiceImpl implements IRobotGroupsService
{
    @Autowired
    private RobotGroupsMapper robotGroupsMapper;
    @Autowired
    private RobotsMapper robotsMapper;

    /**
     * 查询机器人分组
     *
     * @param id 机器人分组主键
     * @return 机器人分组
     */
    @Override
    public RobotGroups selectRobotGroupsById(Long id)
    {
        return robotGroupsMapper.selectRobotGroupsById(id);
    }

    /**
     * 查询机器人分组列表
     *
     * @param robotGroups 机器人分组
     * @return 机器人分组
     */
    @Override
    public List<RobotGroups> selectRobotGroupsList(RobotGroups robotGroups)
    {
        return robotGroupsMapper.selectRobotGroupsList(robotGroups);
    }

    /**
     * 新增机器人分组
     *
     * @param robotGroups 机器人分组
     * @return 结果
     */
    @Override
    public int insertRobotGroups(RobotGroups robotGroups)
    {
        int count = robotGroupsMapper.selectRobotGroupsByName(robotGroups.getName());
        if(count>0)throw new InsertNoAllowedException(ROBOT_CODE_HAS_EXISTED);
        robotGroups.setCreateTime(new Date());
        robotGroups.setUpdateTime(new Date());
        return robotGroupsMapper.insertRobotGroups(robotGroups);
    }

    /**
     * 修改机器人分组
     *
     * @param robotGroups 机器人分组
     * @return 结果
     */
    @Override
    public int updateRobotGroups(RobotGroups robotGroups)
    {
        int count = robotGroupsMapper.selectRobotGroupsByName(robotGroups.getName());
        if(count>0)throw new InsertNoAllowedException(GROUP_NAME_HAS_EXISTED);
        robotGroups.setUpdateTime(new Date());
        return robotGroupsMapper.updateRobotGroups(robotGroups);
    }

    /**
     * 批量删除机器人分组
     *
     * @param ids 需要删除的机器人分组主键
     * @return 结果
     * 删除前确认分组下是否有机器人
     */
    @Override
    public int deleteRobotGroupsByIds(Long[] ids)
    {
        for(Long id : ids)
        {
            Integer count=robotsMapper.countByGroupId(id);
            if(count>0){
                throw new DeleteNoAllowedException(RobotsConstants.GROUP_NAME_HAS_EXISTED);
            }
        }
        return robotGroupsMapper.deleteRobotGroupsByIds(ids);
    }

    /**
     * 删除机器人分组信息
     *
     * @param id 机器人分组主键
     * @return 结果
     */
    @Override
    public int deleteRobotGroupsById(Long id)
    {
        Integer count=robotsMapper.countByGroupId(id);
        if(count>0){
            throw new DeleteNoAllowedException(RobotsConstants.GROUP_BY_RELATED_BY_ROBOT);
        }
        return robotGroupsMapper.deleteRobotGroupsById(id);
    }
}
