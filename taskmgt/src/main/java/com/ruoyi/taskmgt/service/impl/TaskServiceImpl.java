package com.ruoyi.taskmgt.service.impl;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.domain.RobotWarnings;
import com.ruoyi.robots.service.IRobotGroupsService;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.TemplateRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.service.ITaskService;
import com.ruoyi.taskmgt.service.vo.RobotStatus;
import com.ruoyi.taskmgt.service.vo.TaskAbnormalVo;
import com.ruoyi.taskmgt.service.vo.TaskVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {
    private final TaskRepository taskRepository;
    private final MessageSourceAccessor messageSourceAccessor;
    private final RedisCache redisUtil;
    private final TemplateRepository templateRepository;
    private final StepReuseService stepReuseService;
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogService;
    private final IRobotsService robotService;
    private final IRobotWarningsService robotWarningsService;
    private final IRobotGroupsService robotGroupsService;

    /**
     * @param task 即将新增的任务
     * @return
     * &#064;description    新增任务
     **/
    @Override
    public TaskVo createTask(Task task) {
        task.setStatus(Task.NOTSTART);
        Task newTask = this.taskRepository.insert(task);
        this.taskLogService.record(
                newTask.getId(),
                null,
                TaskLogEventType.TASK_CREATE,
                "创建任务：" + task.getName(),
                SecurityUtils.getUsername()
        );
        TaskVo taskVo = CloneFactory.copy(new TaskVo(), newTask);
        taskVo.setTemplateName(this.templateRepository.getTemplateNameById(task.getTemplateId()));
        return taskVo;
    }

    /**
     * @param task 保存了修改信息的任务
     **/
    @Override
    public void updateTask(Task task) {
        if(!Objects.equals(task.getStatus(), Task.DISABLED)){
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString(), task.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW,args,this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }
        else {
            task.setUpdateBy(SecurityUtils.getUsername());
            List<String> redisKeys = this.taskRepository.update(task);
            this.redisUtil.deleteObject(redisKeys);
        }
    }

    /**
     * @param task   任务
     * @param status 需要更改到的状态1
     * @return 修改后任务的redisKey
     * &#064;description   更新任务的状态
     **/
    private List<String> updateTaskStatus(Task task, Byte status) {
        if (Objects.nonNull(task) && task.allowTransitStatus(status)) {
            task.setStatus(status);
            return this.taskRepository.update(task);
        } else {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString(), task.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW,args,this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }
    }

    /**
     * 删除任务（被禁用的任务才能删除）
     * @param id 要删除的任务的id
     **/
    @Override
    public void deleteTask(Long id) {
        Task task = this.taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });

        List<String> redisKeys = this.updateTaskStatus(task,Task.DELETED);
        this.taskLogService.record(
                id,
                null,
                TaskLogEventType.TASK_DELETE,
                " 任务" + task.getName() + "已删除 终止原因：",
                SecurityUtils.getUsername()
        );
        if(StringUtils.isNotNull(task.getTemplateId())){
            Set<String> redisStepKeys = this.stepRepository.deleteStepsByTaskId(id);
            redisKeys.addAll(redisStepKeys);
        }
        this.redisUtil.deleteObject(redisKeys);

    }

    /**
     * 查询任务列表，
     */
    @Override
    public List<TaskVo> retrieveTasks(Byte status, Integer isGroupTask, String name, Long robotId, Long robotGroupId, Integer taskType, Integer riskLevel, Long templateId) {
        List<Task> tasks = this.taskRepository.getTasks(status, isGroupTask, name, robotId, robotGroupId, taskType, riskLevel, templateId);
        if(StringUtils.isNull(isGroupTask)){
            if (StringUtils.isNotNull(robotId)){
                Robot robot=this.robotService.selectRobotsById(robotId);
                robotGroupId= robot.getGroupId();
                tasks.addAll(this.taskRepository.getTasks(status,null,name, null, robotGroupId, taskType, riskLevel, templateId));
            }
            else if(StringUtils.isNotNull(robotGroupId)){
                List<Long> robotIds = new ArrayList<>();
                Robot robot=new Robot();
                robot.setGroupId(robotGroupId);
                List<Robot> robots=this.robotService.selectRobotsList(robot);
                for(Robot bot:robots){
                    robotIds.add(bot.getId());
                }
                tasks.addAll(this.taskRepository.getTasksByRobotIds(status,null,name, robotIds, null, taskType, riskLevel, templateId));
            }
        }
        return tasks.stream()
                .map(task -> {
                    TaskVo taskVo = CloneFactory.copy(new TaskVo(), task);
                    if(StringUtils.isNotNull(task.getTemplateId())){
                        String templateName = this.templateRepository.getTemplateNameById(task.getTemplateId());
                        taskVo.setTemplateName(templateName);
                    }
                    if (StringUtils.isNotNull(task.getRobotId())){
                        String robotName = this.robotService.selectRobotsById(task.getRobotId()).getName();
                        taskVo.setRobotName(robotName);
                    }
                    if (StringUtils.isNotNull(task.getRobotGroupId())) {
                        String robotGroupName = this.robotGroupsService.selectRobotGroupsById(task.getRobotGroupId()).getName();
                        taskVo.setRobotGroupName(robotGroupName);
                    }
                    log.info("TaskVo: {}", taskVo);
                    return taskVo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询任务详情
     * @param id 任务id
     * @return taskVo 任务Vo
     */
    @Override
    public TaskVo getTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        TaskVo taskVo = CloneFactory.copy(new TaskVo(),task);
        if(StringUtils.isNotNull(task.getTemplateId()))taskVo.setTemplateName(this.templateRepository.getTemplateNameById(task.getTemplateId()));
        if (StringUtils.isNotNull(task.getRobotId())){taskVo.setRobotName(this.robotService.selectRobotsById(task.getRobotId()).getName());}
        if (StringUtils.isNotNull(task.getRobotGroupId())) {taskVo.setRobotGroupName(this.robotGroupsService.selectRobotGroupsById(task.getRobotGroupId()).getName());}
        return taskVo;
    }

    /**
     * 禁用任务
     * @param id
     */
    @Override
    public void banTask(Long id){
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        List<String> redisKeys = this.updateTaskStatus(task,Task.DISABLED);
        this.redisUtil.deleteObject(redisKeys);
    }

    /**
     * 恢复任务
     * @param id 被恢复的任务的id
     */
    @Override
    public void resumeTask(Long id){
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        if(!Objects.equals(task.getStatus(), Task.DISABLED)){
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString(), task.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW,args,this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }
        List<String> redisKeys = this.updateTaskStatus(task,Task.NOTSTART);
        this.redisUtil.deleteObject(redisKeys);
    }

    /**
     * 暂停任务
     * @param id 任务的id
     */
    @Override
    public void pauseTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        List<String> redisKeys = this.updateTaskStatus(task,Task.PAUSED);
        this.taskLogService.record(
                id,
                null,
                TaskLogEventType.TASK_PAUSE,
                " 任务" + task.getName() + "已暂停",
                SecurityUtils.getUsername()
        );
        if(StringUtils.isNotNull(task.getTemplateId())){
            List<String> stepRedisKeys = this.stepReuseService.pauseStepsByTaskId(id);
            redisKeys.addAll(stepRedisKeys);
        }
        this.redisUtil.deleteObject(redisKeys);
    }

    /**
     * 继续任务
     * @param id 被暂停的任务的id
     */
    @Override
    public void continueTask(Long id) {
        Task task = this.taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        List<String> redisKeys;
        List<Task>tasks = new ArrayList<>();
        if(task.getIsGroupTask().equals(0)){
            tasks.addAll(this.taskRepository.findByRobotIdAndStatus(task.getRobotId(),Task.EXECUTING));
        }
        else{
            //组任务需确认该组所有的机器人都没有正在执行的任务
            Robot robot = new Robot();
            robot.setGroupId(task.getRobotGroupId());
            List<Robot> robots = this.robotService.selectRobotsList(robot);
            for(Robot bot : robots){
                tasks.addAll(this.taskRepository.findByRobotIdAndStatus(bot.getId(),Task.EXECUTING));
            }

        }
        if (StringUtils.isEmpty(tasks)){
            redisKeys = this.updateTaskStatus(task,Task.EXECUTING);
            this.taskLogService.record(
                    id,
                    null,
                    TaskLogEventType.TASK_RESUME,
                    " 任务" + task.getName() + "已继续",
                    SecurityUtils.getUsername()
            );
            if(StringUtils.isNotNull(task.getTemplateId())){
                List<String> stepRedisKeys = this.stepReuseService.continueStepsByTaskId(id);
                redisKeys.addAll(stepRedisKeys);
            }
        }
        else redisKeys = this.updateTaskStatus(task,Task.PENDING);
        this.redisUtil.deleteObject(redisKeys);
    }

    /**
     * 停止任务
     * @param id 任务的id
     */
    @Override
    public void terminateTask(Long id,String terminateReason) {
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        task.setTerminateReason(terminateReason);
        List<String> redisKeys = this.updateTaskStatus(task,Task.TERMINATED);
        this.taskLogService.record(
                id,
                null,
                TaskLogEventType.TASK_TERMINATE,
                " 任务" + task.getName() + "已终止 终止原因："+terminateReason,
                SecurityUtils.getUsername()
        );
        if(StringUtils.isNotNull(task.getTemplateId())){
            List<String> stepRedisKeys = this.stepReuseService.terminatedStepsByTaskId(id);
            redisKeys.addAll(stepRedisKeys);
        }
        this.redisUtil.deleteObject(redisKeys);
    }

    /**
     * 取消准备中的任务
     * @param id 准备中任务的id
     */
    @Override
    public void cancelTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        List<String> redisKeys = this.updateTaskStatus(task,Task.NOTSTART);
        this.taskLogService.record(
                id,
                null,
                TaskLogEventType.TASK_CANCEL,
                " 任务" + task.getName() + "已取消",
                SecurityUtils.getUsername()
        );
        this.redisUtil.deleteObject(redisKeys);
    }

    @Override
    public void resolveRisk(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    String[] args = {messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
                            messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });

        if (Objects.equals(task.getStatus(), Task.DELETED)) {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString(), task.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW,args,this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }

        boolean allNormal;
        if (task.getIsGroupTask() == 0) {
            allNormal = this.robotWarningsService.countUnresolvedByRobotId(task.getRobotId()) == 0;
        } else {
            Robot robot = new Robot();
            robot.setGroupId(task.getRobotGroupId());
            List<Robot>robots = this.robotService.selectRobotsList(robot);
            List<Long> robotIds = robots.stream().map(Robot::getId).toList();
            allNormal = robotIds.stream()
                    .allMatch(rid -> robotWarningsService.countUnresolvedByRobotId(rid) == 0);
        }

        if (!allNormal) {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString()};
            throw new TaskmgtException(ReturnNo.ROBOT_STATUS_ABNORMAL,args,this.messageSourceAccessor.getMessage(ReturnNo.ROBOT_STATUS_ABNORMAL.getMessage()));
        }

        task.setRiskLevel(0);
        task.setUpdateBy(SecurityUtils.getUsername());
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogService.record(taskId, null, "RISK_RESOLVED",
                "管理员手动解决任务风险", SecurityUtils.getUsername());
    }

    @Override
    public List<TaskAbnormalVo> getAbnormalTasks(Integer riskLevel, Long robotId, Long robotGroupId) {
        List<Task> tasks = taskRepository.getTasks(null, null, null, robotId, robotGroupId, null, riskLevel, null);
        return tasks.stream().map(this::buildAbnormalVo).collect(Collectors.toList());
    }

    private TaskAbnormalVo buildAbnormalVo(Task task) {
        TaskAbnormalVo vo = CloneFactory.copy(new TaskAbnormalVo(), task);
        if (task.getTemplateId() != null) {
            vo.setTemplateName(templateRepository.getTemplateNameById(task.getTemplateId()));
        }

        if (task.getIsGroupTask() == 0) {
            // 单个机器人
            RobotWarnings robotWarning = new RobotWarnings();
            robotWarning.setRobotId(task.getRobotId());
            List<RobotWarnings> warnings = robotWarningsService.selectRobotWarningsList(robotWarning);
            RobotStatus rs = new RobotStatus();
            rs.setRobotId(task.getRobotId());
            rs.setRobotName(robotService.selectRobotsById(task.getRobotId()).getName());

            if (warnings.isEmpty()) {
                rs.setStatus("normal");
                vo.setRobotStatusSummary("正常");
            } else {
                // 取第一个预警类型作为摘要（可根据业务调整）
                String summary = warnings.get(0).getWarningType();
                rs.setStatus(summary);
                vo.setRobotStatusSummary(summary+"预警级别：" + warnings.get(0).getWarningLevel());
            }
            vo.setRobotStatuses(List.of(rs));
        } else {
            // 组任务
            Robot robot = new Robot();
            robot.setGroupId(task.getRobotGroupId());
            List<Robot> robots = this.robotService.selectRobotsList(robot);
            List<RobotStatus> robotStatuses = new ArrayList<>();
            boolean groupHasWarning = false;

            for (Robot bot : robots) {
                RobotWarnings robotWarning = new RobotWarnings();
                robotWarning.setRobotId(bot.getId());
                List<RobotWarnings> warnings = robotWarningsService.selectRobotWarningsList(robotWarning);
                RobotStatus rs = new RobotStatus();
                rs.setRobotId(bot.getId());
                rs.setRobotName(bot.getName());

                if (warnings.isEmpty()) {
                    rs.setStatus("normal");
                } else {
                    groupHasWarning = true;
                    String summary = warnings.get(0).getWarningType();
                    rs.setStatus(summary);
                }
                robotStatuses.add(rs);
            }

            // 组摘要：如果组内有任何预警则显示“组内异常”，否则“正常”
            vo.setRobotStatusSummary(groupHasWarning ? "组内异常" : "正常");
            vo.setRobotStatuses(robotStatuses);
        }
        return vo;
    }

    private List<Long> getMockRobotIdsByGroupId(Long groupId) { return List.of(1L, 2L); }
}
