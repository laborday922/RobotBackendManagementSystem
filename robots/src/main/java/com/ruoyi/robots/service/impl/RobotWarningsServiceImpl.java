package com.ruoyi.robots.service.impl;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.controller.dto.RobotWarningsDto;
import com.ruoyi.robots.event.RobotWarningEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.ruoyi.robots.mapper.RobotWarningsMapper;
import com.ruoyi.robots.domain.RobotWarnings;
import com.ruoyi.robots.service.IRobotWarningsService;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

/**
 * 机器人状态预警Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@Service
public class RobotWarningsServiceImpl implements IRobotWarningsService 
{
    @Autowired
    private RobotWarningsMapper robotWarningsMapper;

    //事件发布
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 查询机器人状态预警
     * 
     * @param id 机器人状态预警主键
     * @return 机器人状态预警
     */
    @Override
    public RobotWarnings selectRobotWarningsById(Long id)
    {
        return robotWarningsMapper.selectRobotWarningsById(id);
    }

    /**
     * 查询机器人状态预警列表
     * 
     * @param robotWarnings 机器人状态预警
     * @return 机器人状态预警
     */
    @Override
    public List<RobotWarnings> selectRobotWarningsList(RobotWarnings robotWarnings)
    {
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            robotWarnings.setTenantId(tenantId);
        return robotWarningsMapper.selectRobotWarningsList(robotWarnings);
    }

    /**
     * 新增机器人状态预警
     * 
     * @param robotWarnings 机器人状态预警
     * @return 结果
     */
    @Override
    public int insertRobotWarnings(RobotWarnings robotWarnings)
    {

        // 发布预警创建事件（状态为待处理）
        eventPublisher.publishEvent(new RobotWarningEvent(
                this,
                robotWarnings.getRobotId(),
                robotWarnings.getWarningType(),
                robotWarnings.getWarningLevel(),
                RobotsConstants.UNRESOLVED,
                true
        ));
        robotWarnings.setTenantId(TenantContext.get());
        return robotWarningsMapper.insertRobotWarnings(robotWarnings);
    }

    /**
     * 修改机器人状态预警
     * 
     * @param robotWarnings 机器人状态预警
     * @return 结果
     */
    @Override
    public int updateRobotWarnings(RobotWarnings robotWarnings)
    {
        return robotWarningsMapper.updateRobotWarnings(robotWarnings);
    }

    /**
     * 批量删除机器人状态预警
     * 
     * @param ids 需要删除的机器人状态预警主键
     * @return 结果
     */
    @Override
    public int deleteRobotWarningsByIds(Long[] ids)
    {
        return robotWarningsMapper.deleteRobotWarningsByIds(ids);
    }

    /**
     * 删除机器人状态预警信息
     * 
     * @param id 机器人状态预警主键
     * @return 结果
     */
    @Override
    public int deleteRobotWarningsById(String id)
    {
        return robotWarningsMapper.deleteRobotWarningsById(id);
    }

    @Override
    public int  dealRobotWarnings(RobotWarningsDto robotWarningsDto) {
        Long[] ids = robotWarningsDto.getIds();
        if ((ids == null || ids.length == 0) && robotWarningsDto.getId() != null) {
            ids = new Long[] { robotWarningsDto.getId() };
        }
        if (ids == null || ids.length == 0) {
            return 0;
        }

        List<RobotWarnings> warningsToDeal = new ArrayList<>();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            RobotWarnings warning = selectRobotWarningsById(id);
            if (warning != null) {
                warningsToDeal.add(warning);
            }
        }
        if (warningsToDeal.isEmpty()) {
            return 0;
        }

        Date resolveTime = new Date();
        Long[] updateIds = warningsToDeal.stream().map(RobotWarnings::getId).toArray(Long[]::new);
        int rows = robotWarningsMapper.dealRobotWarningsByIds(updateIds, resolveTime, robotWarningsDto.getResolveUser(), robotWarningsDto.getResolveNote());

        Map<Long, Boolean> hasRemainingByRobotId = new HashMap<>();
        for (RobotWarnings warning : warningsToDeal) {
            hasRemainingByRobotId.computeIfAbsent(warning.getRobotId(), robotId -> countUnresolvedByRobotId(robotId) != 0);
        }
        for (RobotWarnings warning : warningsToDeal) {
            boolean hasRemaining = hasRemainingByRobotId.getOrDefault(warning.getRobotId(), false);
            eventPublisher.publishEvent(new RobotWarningEvent(
                    this,
                    warning.getRobotId(),
                    warning.getWarningType(),
                    warning.getWarningLevel(),
                    RobotsConstants.RESOLVED,
                    hasRemaining
            ));
        }

        return rows;
    }

    @Override
    public List<RobotWarnings> selectRobotWarningsListByStatus(String status) {
        RobotWarnings robotWarnings = new RobotWarnings();
        robotWarnings.setStatus(status);
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            robotWarnings.setTenantId(tenantId);
        return robotWarningsMapper.selectRobotWarningsList(robotWarnings);
    }

    @Override
    public int countUnresolvedByRobotId(Long robotId) {
        RobotWarnings query = new RobotWarnings();
        query.setRobotId(robotId);
        query.setStatus(RobotsConstants.UNRESOLVED);
        return robotWarningsMapper.selectRobotWarningsList(query).size();
    }
}
