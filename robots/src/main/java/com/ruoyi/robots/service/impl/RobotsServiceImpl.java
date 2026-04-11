package com.ruoyi.robots.service.impl;

import java.util.Date;
import java.util.List;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.controller.dto.RobotsDto;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.domain.RobotWarnings;
import com.ruoyi.robots.exception.InsertNoAllowedException;
import com.ruoyi.robots.mapper.RobotWarningsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.robots.mapper.RobotsMapper;
import com.ruoyi.robots.service.IRobotsService;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;
import static com.ruoyi.robots.common.RobotsConstants.ROBOT_CODE_HAS_EXISTED;

/**
 * 机器人基础信息Service业务层处理
 *
 * @author xiaocai
 * @date 2026-03-07
 */
@Service
public class RobotsServiceImpl implements IRobotsService {
    @Autowired
    private RobotsMapper robotsMapper;
    @Autowired
    RobotWarningsMapper robotWarningsMapper;

    /**
     * 查询机器人基础信息
     *
     * @param id 机器人基础信息主键
     * @return 机器人基础信息
     */
    @Override
    public Robot selectRobotsById(Long id) {
        return robotsMapper.selectRobotsById(id);
    }

    /**
     * 查询机器人基础信息列表
     *
     * @param robot 机器人基础信息
     * @return 机器人基础信息
     */
    @Override
    public List<Robot> selectRobotsList(Robot robot) {
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            robot.setTenantId(tenantId);
        return robotsMapper.selectRobotsList(robot);
    }

    /**
     * 新增机器人基础信息
     *
     * @return 结果
     */
    @Override
    public int insertRobots(RobotsDto robotsDto) {
        Robot robot = new Robot();
        BeanUtils.copyProperties(robotsDto, robot);
        int count = robotsMapper.selectRobotsByCode(robot.getCode());
        if (count > 0) throw new InsertNoAllowedException(ROBOT_CODE_HAS_EXISTED);
        robot.setTenantId(TenantContext.get());
        return robotsMapper.insertRobots(robot);
    }

    /**
     * 修改机器人基础信息
     *
     * @return 结果
     */
    @Override
    public int updateRobots(RobotsDto robotsDto) {
        Robot robot = new Robot();
        BeanUtils.copyProperties(robotsDto, robot);
        return robotsMapper.updateRobots(robot);
    }

/**
 * 机器人状态更新及机器人预警生成
 * @param robotStatusDto 包含机器人状态信息的DTO对象
 * @return 返回更新的记录数
 * @throws InsertNoAllowedException 当机器人ID不存在时抛出异常
 */
    @Override
    public int updateRobotStatus(RobotStatusDto robotStatusDto) {
    // 检查机器人是否存在，不存在则抛出异常
        if(robotsMapper.selectRobotsById(robotStatusDto.getId())==null)
            throw new InsertNoAllowedException(RobotsConstants.ROBOT_ID_NO_EXIST);

    // 创建Robot对象并将DTO属性复制到Robot对象
        Robot robot = new Robot();
        BeanUtils.copyProperties(robotStatusDto, robot);
    // 如果机器人正在执行任务且状态变为更高，设置空闲开始时间
        if (robotsMapper.selectTaskStatusById(robot.getId()) == RobotsConstants.EXECUTING && robot.getStatus() > RobotsConstants.EXECUTING)
            robot.setIdleStartTime(DateUtils.getNowDate());

    // 如果硬件状态正常且机器人状态变为更高，记录警告信息
         if(robotsMapper.selectHardwareStatusById(robot.getId()) == RobotsConstants.NORMAL && robot.getHardwareStatus() > RobotsConstants.NORMAL)
         {
        // 创建警告对象并设置基本信息
             RobotWarnings robotWarnings = new RobotWarnings();
             robotWarnings.setRobotId(robot.getId());
             robotWarnings.setWarningContent(robotStatusDto.getNote());
        // 如果电量低于等于20，设置低电量警告
             if(robotStatusDto.getBattery()<=20)
             {
                 robotWarnings.setWarningType("0");//低电量
                 robotWarnings.setWarningLevel(RobotsConstants.PROMPT);//提示
             }
        // 如果有硬件状态异常，设置硬件警告
             if(robotStatusDto.getHardwareStatus()>0)
             {
                 robotWarnings.setWarningType(String.valueOf(robotStatusDto.getHardwareStatus()));
                 robotWarnings.setWarningLevel(String.valueOf(robotStatusDto.getHardwareStatus()));//警告
             }
        // 设置警告的其他属性并插入数据库
             robotWarnings.setStatus("0");//未完成
             robotWarnings.setCreatedAt(DateUtils.getNowDate());
             robotWarningsMapper.insertRobotWarnings(robotWarnings);
         }
    // 更新机器人信息并返回更新的记录数
        return robotsMapper.updateRobots(robot);
    }

    /**
     * 批量删除机器人基础信息
     *
     * @param ids 需要删除的机器人基础信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotsByIds(String[] ids) {
        return robotsMapper.deleteRobotsByIds(ids);
    }

    /**
     * 删除机器人基础信息信息
     *
     * @param id 机器人基础信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotsById(String id) {
        return robotsMapper.deleteRobotsById(id);
    }

    @Override
    public int updateOnlineStatus(Long robotId, boolean online) {
        Robot robot = new Robot();
        robot.setId(robotId);
        robot.setStatus(online?1:0);
        robot.setLastHeartbeatTime(online?new Date():new Date(0));
        return robotsMapper.updateRobots(robot);
    }

    @Override
    public int updateHeartbeatTime(Long robotId) {
        Robot robot = new Robot();
        robot.setId(robotId);
        robot.setLastHeartbeatTime(new Date());
        return robotsMapper.updateRobots(robot);
    }
}
