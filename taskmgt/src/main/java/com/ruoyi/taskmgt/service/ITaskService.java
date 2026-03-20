package com.ruoyi.taskmgt.service;

import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.service.vo.TaskVo;
import com.ruoyi.taskmgt.service.vo.TaskAbnormalVo;

import javax.transaction.Transactional;
import java.util.List;

public interface ITaskService {
    /**
     * @param task 即将新增的任务
     * @return
     * &#064;description    新增任务
     **/
    TaskVo createTask(Task task);

    /**
     * @param task 保存了修改信息的任务
     **/
    void updateTask(Task task);

    /**
     * 删除任务（被禁用的任务才能删除）
     * @param id 要删除的任务的id
     **/
    void deleteTask(Long id);

    /**
     * 查询任务列表，
     */
    List<TaskVo> retrieveTasks(Byte status, Integer isGroupTask, String name, Long robotId, Long robotGroupId, Integer taskType, Integer riskLevel, Long templateId);

    /**
     * 查询任务详情
     * @param id 任务id
     * @return taskVo 任务Vo
     */
    TaskVo getTask(Long id);

    /**
     * 禁用任务
     * @param id 任务id
     */
    void banTask(Long id);

    /**
     * 恢复任务
     * @param id 被恢复的任务的id
     */
    void resumeTask(Long id);

    /**
     * 暂停任务
     * @param id 任务的id
     */
    void pauseTask(Long id);

    /**
     * 继续任务
     * @param id 被暂停的任务的id
     */
    void continueTask(Long id);

    /**
     * 停止任务
     * @param id 任务的id
     */
    void terminateTask(Long id, String terminateReason);

    /**
     * 取消准备中的任务
     * @param id 准备中任务的id
     */
    void cancelTask(Long id);

    @Transactional
    void updateGlobalOrder(List<Long> taskIds);

    @Transactional
    void updateLocalOrder(Long resourceId, boolean isGroupTask, List<Long> taskIds);

    void validateLocalOrder(List<Task> tasks);

    List<TaskAbnormalVo> getAbnormalTasks(Integer riskLevel, Long robotId, Long robotGroupId);

    boolean resolveRisk(Long id);

    TaskAbnormalVo getAbnormalTask(Long id);
}
