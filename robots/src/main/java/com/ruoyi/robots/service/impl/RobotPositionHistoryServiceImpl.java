package com.ruoyi.robots.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.mapper.RobotsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.robots.mapper.RobotPositionHistoryMapper;
import com.ruoyi.robots.domain.RobotPositionHistory;
import com.ruoyi.robots.service.IRobotPositionHistoryService;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

/**
 * 机器人位置历史信息Service业务层处理
 *
 * @author xiaocai
 * @date 2026-03-07
 */
@Service
public class RobotPositionHistoryServiceImpl implements IRobotPositionHistoryService
{
    @Autowired
    private RobotPositionHistoryMapper robotPositionHistoryMapper;
    @Autowired
    private RobotsMapper robotsMapper;


    /**
     * 查询机器人位置历史信息
     *
     * @param id 机器人位置历史信息主键
     * @return 机器人位置历史信息
     */
    @Override  // 标记重写父类方法
    public RobotPositionHistory selectRobotPositionHistoryById(Long id)  // 方法声明：根据ID查询机器人位置历史信息
    {
        return robotPositionHistoryMapper.selectRobotPositionHistoryById(id);
    }

    /**
     * 查询机器人位置历史信息列表
     *
     * @param robotPositionHistory 机器人位置历史信息
     * @return 机器人位置历史信息
     */
    @Override
    public List<RobotPositionHistory> selectRobotPositionHistoryList(RobotPositionHistory robotPositionHistory)
    {
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            robotPositionHistory.setTenantId(tenantId);
        return robotPositionHistoryMapper.selectRobotPositionHistoryList(robotPositionHistory);
    }

    /**
     * 新增机器人位置历史信息
     *
     * @param robotPositionHistory 机器人位置历史信息
     * @return 结果
     */
    @Override
    public int insertRobotPositionHistory(RobotPositionHistory robotPositionHistory)
    {
        robotPositionHistory.setTenantId(TenantContext.get());
        return robotPositionHistoryMapper.insertRobotPositionHistory(robotPositionHistory);
    }

    /**
     * 修改机器人位置历史信息
     *
     * @param robotPositionHistory 机器人位置历史信息
     * @return 结果
     */
    @Override
    public int updateRobotPositionHistory(RobotPositionHistory robotPositionHistory)
    {
        return robotPositionHistoryMapper.updateRobotPositionHistory(robotPositionHistory);
    }

    /**
     * 批量删除机器人位置历史信息
     *
     * @param ids 需要删除的机器人位置历史信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotPositionHistoryByIds(Long[] ids)
    {
        return robotPositionHistoryMapper.deleteRobotPositionHistoryByIds(ids);
    }

    /**
     * 删除机器人位置历史信息信息
     *
     * @param id 机器人位置历史信息主键
     * @return 结果
     */
    @Override
    public int deleteRobotPositionHistoryById(Long id)
    {
        return robotPositionHistoryMapper.deleteRobotPositionHistoryById(id);
    }

    @Override
    public void saveIfPositionChanged(RobotStatusDto robotStatusDto) {
        if (robotStatusDto == null || robotStatusDto.getId() == null) {
            return;
        }

        RobotPositionHistory incoming = new RobotPositionHistory();
        BeanUtils.copyProperties(robotStatusDto, incoming);
        incoming.setId(null);
        incoming.setRobotId(robotStatusDto.getId());
        incoming.setRecordTime(robotStatusDto.getLastHeartbeatTime());

        // 未携带任何位置信息时不写入历史表
        if (incoming.getLocationArea() == null && incoming.getSpecificLocation() == null
                && incoming.getCoordinateX() == null && incoming.getCoordinateY() == null
                && incoming.getMoveSpeed() == null) {
            return;
        }

        RobotPositionHistory latest = robotPositionHistoryMapper.selectLatestRobotPositionHistoryByRobotId(robotStatusDto.getId());
        if (isPositionChanged(latest, incoming)) {
            robotPositionHistoryMapper.insertRobotPositionHistory(incoming);
        }
    }

    /**
     * 判断位置相关信息是否发生变化
     */
    private boolean isPositionChanged(RobotPositionHistory latest, RobotPositionHistory incoming) {
        if (latest == null) {
            return true;
        }
        if (!Objects.equals(latest.getLocationArea(), incoming.getLocationArea())) {
            return true;
        }
        if (!Objects.equals(latest.getSpecificLocation(), incoming.getSpecificLocation())) {
            return true;
        }
        if (!decimalEquals(latest.getCoordinateX(), incoming.getCoordinateX())) {
            return true;
        }
        if (!decimalEquals(latest.getCoordinateY(), incoming.getCoordinateY())) {
            return true;
        }
        if (!decimalEquals(latest.getMoveSpeed(), incoming.getMoveSpeed())) {
            return true;
        }
        return false;
    }

    /**
     * BigDecimal 按数值比较，忽略精度差异
     */
    private boolean decimalEquals(BigDecimal left, BigDecimal right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }
}
