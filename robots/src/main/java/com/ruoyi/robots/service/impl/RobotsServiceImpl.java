package com.ruoyi.robots.service.impl;

import java.util.List;

import com.ruoyi.robots.controller.dto.RobotsDto;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.exception.InsertNoAllowedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.robots.mapper.RobotsMapper;
import com.ruoyi.robots.service.IRobotsService;

import static com.ruoyi.robots.common.RobotsConstants.ROBOT_CODE_HAS_EXISTED;

/**
 * 机器人基础信息Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@Service
public class RobotsServiceImpl implements IRobotsService 
{
    @Autowired
    private RobotsMapper robotsMapper;

    /**
     * 查询机器人基础信息
     * 
     * @param id 机器人基础信息主键
     * @return 机器人基础信息
     */
    @Override
    public Robot selectRobotsById(Long id)
    {
        return robotsMapper.selectRobotsById(id);
    }

    /**
     * 查询机器人基础信息列表
     * 
     * @param robot 机器人基础信息
     * @return 机器人基础信息
     */
    @Override
    public List<Robot> selectRobotsList(Robot robot)
    {
        return robotsMapper.selectRobotsList(robot);
    }

    /**
     * 新增机器人基础信息
     * 
     * @param robot 机器人基础信息
     * @return 结果
     */
    @Override
    public int insertRobots(Robot robot)
    {
        int count=robotsMapper.selectRobotsByCode(robot.getCode());
        if(count>0)throw new InsertNoAllowedException(ROBOT_CODE_HAS_EXISTED);
        return robotsMapper.insertRobots(robot);
    }

    /**
     * 修改机器人基础信息
     *
     * @return 结果
     */
    @Override
    public int updateRobots(RobotsDto robotsDto)
    {
        int count=robotsMapper.selectRobotsByCode(robotsDto.getCode());
        if(count>0)throw new InsertNoAllowedException(ROBOT_CODE_HAS_EXISTED);
        Robot robot = new Robot();
        BeanUtils.copyProperties(robotsDto, robot);
        return robotsMapper.updateRobots(robot);
    }

    /**
     * 批量删除机器人基础信息
     * 
     * @param ids 需要删除的机器人基础信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotsByIds(String[] ids)
    {
        return robotsMapper.deleteRobotsByIds(ids);
    }

    /**
     * 删除机器人基础信息信息
     * 
     * @param id 机器人基础信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotsById(String id)
    {
        return robotsMapper.deleteRobotsById(id);
    }
}
