package com.ruoyi.taskmgt.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import com.ruoyi.taskmgt.domain.vo.SumOfInteractionHistoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.taskmgt.mapper.TInteractionHistoryMapper;
import com.ruoyi.taskmgt.domain.TInteractionHistory;
import com.ruoyi.taskmgt.service.ITInteractionHistoryService;

/**
 * 交互历史记录Service业务层处理
 *
 * @author xiaocai
 * @date 2026-03-13
 */
@Service
public class TInteractionHistoryServiceImpl implements ITInteractionHistoryService
{
    @Autowired
    private TInteractionHistoryMapper tInteractionHistoryMapper;

    @Autowired
    private TaskLogRepository taskLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 查询交互历史记录
     *
     * @param id 交互历史记录主键
     * @return 交互历史记录
     */
    @Override
    public TInteractionHistory selectTInteractionHistoryById(Long id)
    {
        return tInteractionHistoryMapper.selectTInteractionHistoryById(id);
    }

    /**
     * 查询交互历史记录列表
     *
     * @param tInteractionHistory 交互历史记录
     * @return 交互历史记录
     */
    @Override
    public List<TInteractionHistory> selectTInteractionHistoryList(TInteractionHistory tInteractionHistory)
    {
        return tInteractionHistoryMapper.selectTInteractionHistoryList(tInteractionHistory);
    }

    /**
     * 新增交互历史记录
     *
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    @Override
    public int insertTInteractionHistory(TInteractionHistory tInteractionHistory)
    {
        return tInteractionHistoryMapper.insertTInteractionHistory(tInteractionHistory);
    }

    /**
     * 修改交互历史记录
     *
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    @Override
    public int updateTInteractionHistory(TInteractionHistory tInteractionHistory)
    {
        return tInteractionHistoryMapper.updateTInteractionHistory(tInteractionHistory);
    }

    /**
     * 批量删除交互历史记录
     *
     * @param ids 需要删除的交互历史记录主键
     * @return 结果
     */
    @Override
    public int deleteTInteractionHistoryByIds(Long[] ids)
    {
        return tInteractionHistoryMapper.deleteTInteractionHistoryByIds(ids);
    }

    /**
     * 删除交互历史记录信息
     *
     * @param id 交互历史记录主键
     * @return 结果
     */
    @Override
    public int deleteTInteractionHistoryById(Long id)
    {
        return tInteractionHistoryMapper.deleteTInteractionHistoryById(id);
    }

    @Override
    public SumOfInteractionHistoryVo sumOfInteractionHistory() {
        SumOfInteractionHistoryVo sumOfInteractionHistoryVo = tInteractionHistoryMapper.sumOfInteractionHistory();
        sumOfInteractionHistoryVo.calculateAverageRating();
        return sumOfInteractionHistoryVo;
    }

    @Override
    public int buildAndSaveEvaluation(String interactionId, Long rating, String evaluationText) {
        // 1. 查询该 interactionId 对应的所有日志（按时间升序）
        List<TaskLog> logs = taskLogRepository.findByInteractionId(interactionId);
        if (logs.isEmpty()) {
            throw new IllegalArgumentException("未找到 interactionId=" + interactionId + " 的任务执行日志");
        }

        // 2. 第一条日志的时间为交互开始时间，最后一条的事件类型决定状态
        TaskLog firstLog = logs.get(0);
        TaskLog lastLog = logs.get(logs.size() - 1);

        // 3. 构建交互历史记录
        TInteractionHistory record = new TInteractionHistory();
        record.setTaskId(firstLog.getTaskId().toString());
        record.setInteractionId(interactionId);
        record.setSourceType(1L); // 1-任务
        record.setInteractionTime(firstLog.getCreateTime());

        // 4. 从任务表获取 robotId 和任务名称
        Optional<Task> taskOpt = taskRepository.findById(firstLog.getTaskId());
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            record.setRobotId(task.getRobotId());
            record.setInteractionContent("完成任务：" + task.getName());
        }

        // 5. 根据最后一条日志的事件类型判断交互状态
        // 成功：最后一条是 TASK_COMPLETE；失败：最后一条是 TASK_TERMINATE
        String lastEventType = lastLog.getEventType();
        if ("TASK_TERMINATE".equals(lastEventType)) {
            record.setStatus(1L); // 失败
        } else if ("TASK_COMPLETE".equals(lastEventType)) {
            record.setStatus(0L); // 成功
        } else {
            record.setStatus(0L); // 默认成功
        }

        // 6. 计算耗时（秒）
        long durationSeconds = (lastLog.getCreateTime().getTime() - firstLog.getCreateTime().getTime()) / 1000;
        record.setDuration(durationSeconds);

        record.setRating(rating);
        record.setEvaluationText(evaluationText);

        return insertTInteractionHistory(record);
    }

    @Override
    public int saveQAEvaluation(Long robotId, Long rating, String evaluationText, Date interactionTime, Long duration) {
        TInteractionHistory record = new TInteractionHistory();
        record.setRobotId(robotId);
        record.setSourceType(2L); // 2-问答
        record.setRating(rating);
        record.setEvaluationText(evaluationText);
        record.setInteractionTime(interactionTime);
        record.setDuration(duration);
        record.setInteractionContent("问答");
        record.setStatus(0L); //0-成功

        return insertTInteractionHistory(record);
    }
}
