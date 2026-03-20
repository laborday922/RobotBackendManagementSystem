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
import java.util.*;
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
        task.setRiskLevel(0);
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
        if(Objects.equals(task.getStatus(), Task.EXECUTING)){
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString(), task.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW,args,this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }
        else {
            if(StringUtils.isNotNull(task.getRobotId())||StringUtils.isNotNull(task.getRobotGroupId())){
                if(!isRobotNormal(task)){
                    String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.toString()};
                    throw new  TaskmgtException(ReturnNo.ROBOT_STATUS_ABNORMAL, args,this.messageSourceAccessor.getMessage(ReturnNo.ROBOT_STATUS_ABNORMAL.getMessage()));
                }
                Task originTask = this.taskRepository.findById(task.getId()).orElseThrow(()-> {
                    String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });
                if (Objects.equals(originTask.getStatus(), Task.PAUSED)){
                    if(StringUtils.isNotNull(task.getRobotId())&& !task.getRobotId().equals(originTask.getRobotId())||StringUtils.isNotNull(task.getRobotGroupId())&& !task.getRobotGroupId().equals(originTask.getRobotGroupId())){
                        task.setStatus(Task.PENDING);
                    }
                }
            }
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
        if(Objects.nonNull(task) && !isRobotNormal(task)){
            if(Objects.equals(status, Task.EXECUTING))task.setRiskLevel(2);
        }
        else if(Objects.nonNull(task) && (Objects.equals(status,Task.PENDING)||Objects.equals(status,Task.PAUSED)))task.setRiskLevel(1);
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
        Set<Task> tasks = new LinkedHashSet<> (this.taskRepository.getTasks(status, isGroupTask, name, robotId, robotGroupId, taskType, riskLevel, templateId));
        if(StringUtils.isNotEmpty(tasks)){
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
                        if (StringUtils.isNotNull(task.getRobotId())) {
                            try {
                                Robot robot = robotService.selectRobotsById(task.getRobotId());
                                if (robot != null) {
                                    taskVo.setRobotName(robot.getName());
                                }
                            } catch (Exception e) {
                                log.error("查询机器人信息失败, robotId: {}", task.getRobotId(), e);
                                taskVo.setRobotName(null);
                            }
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
        else return List.of();
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
        log.info("getTask id: {}, robotId: {}", id, task.getRobotId());
        if(StringUtils.isNotNull(task.getTemplateId()))taskVo.setTemplateName(this.templateRepository.getTemplateNameById(task.getTemplateId()));
        if (StringUtils.isNotNull(task.getRobotId())){taskVo.setRobotName(this.robotService.selectRobotsById(task.getRobotId()).getName());}
        if (StringUtils.isNotNull(task.getRobotGroupId())) {taskVo.setRobotGroupName(this.robotGroupsService.selectRobotGroupsById(task.getRobotGroupId()).getName());}
        return taskVo;
    }

    /**
     * 禁用任务
     * @param id 任务id
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
     * @param id 被禁用的任务的id
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
    public boolean resolveRisk(Long taskId) {
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
            if(!(Objects.equals(task.getStatus(), Task.TERMINATED)||Objects.equals(task.getStatus(),Task.NOTSTART)))return false;
        }

        task.setRiskLevel(0);
        task.setUpdateBy(SecurityUtils.getUsername());
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogService.record(taskId, null, "RISK_RESOLVED",
                "管理员手动解决任务风险", SecurityUtils.getUsername());
        return true;
    }

    @Override
    public TaskAbnormalVo getAbnormalTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        return buildAbnormalVo(task);
    }


    @Override
    @Transactional
    public void updateGlobalOrder(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return;

        List<Task> tasks = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null);
        Map<Long, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));
        if (taskMap.size() != taskIds.size()) {
            String[] args = new String[]{};
            throw new TaskmgtException(ReturnNo.DATA_INVALID, args, this.messageSourceAccessor.getMessage(ReturnNo.DATA_INVALID.getMessage()));
        }

        // 按资源分组：机器人（单任务）和机器人组（组任务）
        Map<Long, List<Task>> robotTasksMap = new HashMap<>();
        Map<Long, List<Task>> groupTasksMap = new HashMap<>();
        for (Task task : tasks) {
            if (task.getIsGroupTask() == 0) {
                robotTasksMap.computeIfAbsent(task.getRobotId(), k -> new ArrayList<>()).add(task);
            } else {
                groupTasksMap.computeIfAbsent(task.getRobotGroupId(), k -> new ArrayList<>()).add(task);
            }
        }

        // 校验每个资源内任务的顺序是否与 pendingOrder 一致（不允许改变资源内顺序）
        for (List<Task> rt : robotTasksMap.values()) {
            validateLocalOrder(rt);
        }
        for (List<Task> gt : groupTasksMap.values()) {
            validateLocalOrder(gt);
        }

        // 重新分配全局顺序
        for (int i = 0; i < taskIds.size(); i++) {
            Long tid = taskIds.get(i);
            Task task = taskMap.get(tid);
            task.setGlobalPendingOrder(i);
        }

        // 批量更新
        for (Task task : tasks) {
            task.setUpdateBy(SecurityUtils.getUsername());
            List<String> redisKeys = taskRepository.update(task);
            if (redisKeys != null && !redisKeys.isEmpty()) {
                redisUtil.deleteObject(redisKeys);
            }
        }
    }

    @Override
    @Transactional
    public void updateLocalOrder(Long resourceId, boolean isGroupTask, List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return;

        // 查询该资源下所有准备中任务
        List<Task> tasks;
        if (isGroupTask) {
            tasks = taskRepository.getTasks(Task.PENDING, 1, null, null, resourceId, null, null, null);
        } else {
            tasks = taskRepository.getTasks(Task.PENDING, 0, null, resourceId, null, null, null, null);
        }

        Set<Long> currentIds = tasks.stream().map(Task::getId).collect(Collectors.toSet());
        Set<Long> inputIds = new HashSet<>(taskIds);
        if (!currentIds.equals(inputIds)) {
            throw new TaskmgtException(ReturnNo.DATA_INVALID, new String[]{}, this.messageSourceAccessor.getMessage(ReturnNo.DATA_INVALID.getMessage()));
        }

        // 重新分配 pendingOrder
        Map<Long, Integer> newPending = new HashMap<>();
        for (int i = 0; i < taskIds.size(); i++) {
            newPending.put(taskIds.get(i), i);
        }
        for (Task task : tasks) {
            task.setPendingOrder(newPending.get(task.getId()));
        }

        // 获取所有准备中任务
        List<Task> allPending = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null);
        Set<Long> currentIdsSet = new HashSet<>(taskIds);

        // 构建新顺序列表：保持非当前资源任务顺序不变，当前资源任务按新 pendingOrder 顺序插入到原位置
        List<Task> newOrder = new ArrayList<>();
        // 按新 pendingOrder 升序迭代当前资源任务
        Iterator<Task> reorderIterator = tasks.stream()
                .sorted(Comparator.comparingInt(Task::getPendingOrder))
                .iterator();

        for (Task task : allPending) {
            if (currentIdsSet.contains(task.getId())) {
                // 当前资源任务，按新顺序插入
                newOrder.add(reorderIterator.next());
            } else {
                newOrder.add(task);
            }
        }

        // 重新分配 globalPendingOrder
        for (int i = 0; i < newOrder.size(); i++) {
            Task task = newOrder.get(i);
            task.setGlobalPendingOrder(i);
            task.setUpdateBy(SecurityUtils.getUsername());
            List<String> redisKeys = taskRepository.update(task);
            if (redisKeys != null && !redisKeys.isEmpty()) {
                redisUtil.deleteObject(redisKeys);
            }
        }
    }
    @Override
    public void validateLocalOrder(List<Task> tasks) {
        List<Long> sortedIds = tasks.stream()
                .sorted(Comparator.comparingInt(Task::getPendingOrder))
                .map(Task::getId)
                .toList();
        List<Long> inputIds = tasks.stream().map(Task::getId).toList();
        if (!sortedIds.equals(inputIds)) {
            String[] args = new String[]{};
            throw new TaskmgtException(ReturnNo.DATA_INVALID, new String[]{}, this.messageSourceAccessor.getMessage(ReturnNo.DATA_INVALID.getMessage()));
        }
    }

    @Override
    public List<TaskAbnormalVo> getAbnormalTasks(Integer riskLevel, Long robotId, Long robotGroupId) {
        List<Task> tasks;
        if(riskLevel!=null&&riskLevel!=0){
            tasks = taskRepository.getTasks(null, null, null, robotId, robotGroupId, null, riskLevel, null);
        }
        else if(riskLevel==null){
            tasks = taskRepository.getTasks(null, null, null, robotId, robotGroupId, null, 1, null);
            tasks.addAll(taskRepository.getTasks(null, null, null, robotId, robotGroupId, null, 2, null));
        }
        else {
            tasks = List.of();
        }
        return tasks.stream().map(this::buildAbnormalVo).collect(Collectors.toList());
    }

    private TaskAbnormalVo buildAbnormalVo(Task task) {
        if(StringUtils.isNull(task)){
            return new TaskAbnormalVo();
        }
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
                String summary = warnings.get(0).getWarningType();
                rs.setStatus(summary);
                vo.setRobotStatusSummary(summary+"预警级别：" + warnings.get(0).getWarningLevel());
            }
            vo.setRobotStatuses(List.of(rs));
            vo.setRobotName(robotService.selectRobotsById(task.getRobotId()).getName());
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
            vo.setRobotGroupName(robotGroupsService.selectRobotGroupsById(task.getRobotGroupId()).getName());
        }
        return vo;
    }

    public boolean isRobotNormal(Task task){
        boolean isNormal = true;
        if(StringUtils.isNotNull(task.getRobotId())) {
            if (this.robotWarningsService.countUnresolvedByRobotId(task.getRobotId())!=0)isNormal=false;
        }
        else if(StringUtils.isNotNull(task.getRobotGroupId())){
            Robot robot = new Robot();
            robot.setGroupId(task.getRobotGroupId());
            List<Robot> robots = this.robotService.selectRobotsList(robot);
            for(Robot bot : robots){
                if(this.robotWarningsService.countUnresolvedByRobotId(bot.getId())!=0){
                    isNormal = false;
                    break;
                }
            }
        }
        return isNormal;
    }
    private List<Long> getMockRobotIdsByGroupId(Long groupId) { return List.of(1L, 2L); }
}
