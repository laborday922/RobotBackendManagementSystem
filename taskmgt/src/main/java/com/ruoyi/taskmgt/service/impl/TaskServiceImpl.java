package com.ruoyi.taskmgt.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ruoyi.app.domain.TAppConstraint;
import com.ruoyi.app.domain.TAppLibrary;
import com.ruoyi.app.domain.TAppParam;
import com.ruoyi.app.service.ITAppConstraintService;
import com.ruoyi.app.service.ITAppLibraryService;
import com.ruoyi.app.service.ITAppParamService;
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
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.domain.bo.Template;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.ITaskService;
import com.ruoyi.taskmgt.service.vo.RobotStatus;
import com.ruoyi.taskmgt.service.vo.TaskAbnormalVo;
import com.ruoyi.taskmgt.service.vo.TaskVo;
import com.ruoyi.taskmgt.utils.ExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.taskmgt.utils.ParamValidator.objectMapper;

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
    private final IStepService stepService;
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogService;
    private final IRobotsService robotService;
    private final IRobotWarningsService robotWarningsService;
    private final IRobotGroupsService robotGroupsService;
    private final ITAppLibraryService appLibraryService;
    private final ITAppParamService appParamService;
    private final ITAppConstraintService constraintService;
    private final ExpressionEvaluator expressionEvaluator;

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
        List<TaskStep> steps = retrieveSteps(newTask);
        stepService.createSteps(newTask.getId(),steps);
        this.taskLogService.record(
                newTask.getId(),
                null,
                TaskLogEventType.TASK_CREATE,
                "创建任务：" + newTask.getName(),
                SecurityUtils.getUsername()
        );
        TaskVo taskVo = CloneFactory.copy(new TaskVo(), newTask);
        taskVo.setTemplateName(this.templateRepository.getTemplateNameById(task.getTemplateId()));
        return taskVo;
    }
    /**
     * @param task 即将新增的任务
     * @return 构造好的步骤
     */
    @Override
    public List<TaskStep> retrieveSteps(Task task) {
        Template template = this.templateRepository.findById(task.getTemplateId()).orElseThrow(() -> {
            String[] args = new String[]{
                    this.messageSourceAccessor.getMessage("Template.name", LocaleContextHolder.getLocale()),
                    task.getTemplateId().toString()
            };
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
                    this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });

        TAppLibrary app = appLibraryService.selectTAppLibraryById(template.getAppId());

        // 获取并转换能力参数
        TAppParam param = new TAppParam();
        param.setAppId(app.getAppId());
        List<TAppParam> appParams = appParamService.selectTAppParamList(param);
        Map<String, Object> finalAppParams = new HashMap<>();

        for (TAppParam appParam : appParams) {
            Object value = convertValueByType(
                    appParam.getDefaultValue(),
                    appParam.getParamType(),
                    appParam.getParamKey() + "(appParam)"
            );
//            if (appParams.isEditableInTask() && task.getAppParamOverrides().containsKey(appParam.getKey())) {
//                value = dto.getAppParamOverrides().get(param.getKey());
//                // 校验覆盖值是否符合validation_rule
//                validateParamValue(param, value);
//            }
            finalAppParams.put(appParam.getParamKey(), value);
        }

        // 获取模板约束规则（应用级 + 模板级）
        List<TAppConstraint> rules = new ArrayList<>();
        TAppConstraint appConstraint = new TAppConstraint();
        appConstraint.setAppId(app.getAppId());
        List<TAppConstraint> appRules = constraintService.selectTAppConstraintList(appConstraint);
        if (StringUtils.isNotEmpty(appRules)) {
            rules.addAll(appRules);
        }
        if (StringUtils.isNotEmpty(template.getRules())) {
            rules.addAll(template.getRules());
        }

        // 解析并转换表单数据
        Map<String, Object> formDataMap = new HashMap<>();
        if (StringUtils.hasText(task.getFormContent())) {
            try {
                Map<String, Object> rawFormData = objectMapper.readValue(
                        task.getFormContent(),
                        new TypeReference<Map<String, Object>>() {}
                );
                // 根据模板字段定义转换类型
                formDataMap = convertFormDataByFieldTypes(rawFormData, template);
            } catch (Exception e) {
                throw new TaskmgtException(ReturnNo.FIELD_NOTVALID, new Object[]{}, "表单数据格式错误: " + e.getMessage());
            }
        }

        // 构建表达式上下文并验证约束
        Map<String, Object> context = new HashMap<>();
        context.put("form_data", formDataMap);
        context.put("app_param", finalAppParams);

        for (TAppConstraint rule : rules) {
            if (!expressionEvaluator.evaluateBoolean(rule.getExpression(), context)) {
                throw new TaskmgtException(ReturnNo.FIELD_NOTVALID, new Object[]{}, rule.getErrorMessage());
            }
        }

        return stepReuseService.insertSteps(template, formDataMap, finalAppParams);
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
            Task originTask = this.taskRepository.findById(task.getId()).orElseThrow(()-> {
                String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString()};
                return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
            });
            if(StringUtils.isNotNull(task.getRobotId())||StringUtils.isNotNull(task.getRobotGroupId())){
                if(!isRobotNormal(task)){
                    String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.toString()};
                    throw new  TaskmgtException(ReturnNo.ROBOT_STATUS_ABNORMAL, args,this.messageSourceAccessor.getMessage(ReturnNo.ROBOT_STATUS_ABNORMAL.getMessage()));
                }
                if (Objects.equals(originTask.getStatus(), Task.PAUSED)){
                    if(StringUtils.isNotNull(task.getRobotId())&& !task.getRobotId().equals(originTask.getRobotId())||StringUtils.isNotNull(task.getRobotGroupId())&& !task.getRobotGroupId().equals(originTask.getRobotGroupId())){
                        task.setStatus(Task.PENDING);
                    }
                }
            }
            if(StringUtils.hasText(task.getFormContent())&&StringUtils.isNotNull(task.getTemplateId())&&Objects.equals(originTask.getStatus(), Task.DISABLED)){
                List<TaskStep> steps = retrieveSteps(task);
                stepService.updateSteps(task.getId(),steps);
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
                            Integer totalSteps = this.stepRepository.findStepsByTaskId(task.getId()).size();
                            Integer completedSteps = this.stepRepository.findStepsByTaskId(task.getId()).stream().filter(step-> Objects.equals(step.getStatus(), TaskStep.FINISHED)).toList().size();
                            taskVo.setTotalSteps(totalSteps);
                            taskVo.setCompletedSteps(completedSteps);
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
        if(StringUtils.isNotNull(task.getTemplateId())){
            List<String> stepRedisKeys = this.stepReuseService.cancelStepsByTaskId(id);
            redisKeys.addAll(stepRedisKeys);
        }
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

        // 获取所有准备中任务
        List<Task> allPendingTasks = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null);
        Map<Long, Task> taskMap = allPendingTasks.stream().collect(Collectors.toMap(Task::getId, t -> t));

        // 校验所有taskId都存在
        if (taskMap.size() != taskIds.size()) {
            throw new TaskmgtException(ReturnNo.DATA_INVALID, new String[]{},
                    this.messageSourceAccessor.getMessage(ReturnNo.DATA_INVALID.getMessage()));
        }

        // 按资源分组当前数据库中的任务
        Map<String, List<Task>> resourceGroups = new HashMap<>();
        for (Task task : allPendingTasks) {
            String key = task.getIsGroupTask() == 0
                    ? "robot-" + task.getRobotId()
                    : "group-" + task.getRobotGroupId();
            resourceGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        }

        // 校验传入的taskIds中，每个资源内的相对顺序必须与pendingOrder一致
        Map<String, List<Long>> inputResourceGroups = new HashMap<>();
        for (Long taskId : taskIds) {
            Task task = taskMap.get(taskId);
            String key = task.getIsGroupTask() == 0
                    ? "robot-" + task.getRobotId()
                    : "group-" + task.getRobotGroupId();
            inputResourceGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(taskId);
        }

        for (Map.Entry<String, List<Long>> entry : inputResourceGroups.entrySet()) {
            String resourceKey = entry.getKey();
            List<Long> inputIdsForResource = entry.getValue();

            // 获取该资源下任务按pendingOrder排序的ID列表
            List<Long> sortedByPendingOrder = resourceGroups.get(resourceKey).stream()
                    .sorted(Comparator.comparingInt(Task::getPendingOrder))
                    .map(Task::getId)
                    .toList();

            // 校验传入的顺序是否与pendingOrder一致
            if (!inputIdsForResource.equals(sortedByPendingOrder)) {
                throw new TaskmgtException(ReturnNo.DATA_INVALID, new String[]{},
                        "资源内任务顺序与pendingOrder不一致，不允许改变资源内相对顺序");
            }
        }

        // 4. 重新分配全局顺序
        for (int i = 0; i < taskIds.size(); i++) {
            Long tid = taskIds.get(i);
            Task task = taskMap.get(tid);
            task.setGlobalPendingOrder(i);
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
            Integer totalSteps = this.stepRepository.findStepsByTaskId(task.getId()).size();
            Integer completedSteps = this.stepRepository.findStepsByTaskId(task.getId()).stream().filter(step-> Objects.equals(step.getStatus(), TaskStep.FINISHED)).toList().size();
            vo.setTotalSteps(totalSteps);
            vo.setCompletedSteps(completedSteps);
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
    /**
     * 根据类型转换值
     * @param value 原始值（通常是字符串）
     * @param type 类型标识：number, integer, date, datetime, time, boolean, string
     * @param fieldName 字段名（用于错误提示）
     * @return 转换后的对象
     */
    private Object convertValueByType(Object value, String type, String fieldName) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }

        String typeLower = type != null ? type.toLowerCase() : "string";
        String valueStr = value.toString().trim();

        try {
            switch (typeLower) {
                case "number":
                case "double":
                case "decimal":
                case "float":
                    // 统一使用 Double 进行数值比较
                    return Double.parseDouble(valueStr);

                case "integer":
                case "int":
                case "long":
                    // 整数类型，但为比较统一也转为 Double，或根据需求用 Integer
                    return Integer.parseInt(valueStr);

                case "boolean":
                case "bool":
                    return Boolean.parseBoolean(valueStr);

                case "date":
                    // 日期类型：yyyy-MM-dd
                    return java.time.LocalDate.parse(valueStr);

                case "datetime":
                case "timestamp":
                    // 日期时间类型：yyyy-MM-dd HH:mm:ss 或 ISO 格式
                    if (valueStr.contains("T")) {
                        return java.time.LocalDateTime.parse(valueStr);
                    } else {
                        return java.time.LocalDateTime.parse(
                                valueStr,
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                    }

                case "time":
                    // 时间类型：HH:mm:ss
                    return java.time.LocalTime.parse(valueStr);

                case "string":
                case "text":
                default:
                    return valueStr;
            }
        } catch (Exception e) {
            throw new TaskmgtException(
                    ReturnNo.FIELD_NOTVALID,
                    new Object[]{fieldName},
                    String.format("字段[%s]类型转换失败: 值'%s'无法转换为类型'%s'", fieldName, valueStr, type)
            );
        }
    }

    /**
     * 根据模板字段定义转换表单数据
     * 从 template.formContent 中解析字段类型定义
     */
    private Map<String, Object> convertFormDataByFieldTypes(
            Map<String, Object> rawFormData,
            Template template
    ) {
        Map<String, Object> convertedData = new HashMap<>();

        // 如果没有原始数据，直接返回
        if (rawFormData == null || rawFormData.isEmpty()) {
            return convertedData;
        }

        // 从模板解析字段定义
        Map<String, String> fieldTypeMap = parseFieldTypesFromTemplate(template);

        for (Map.Entry<String, Object> entry : rawFormData.entrySet()) {
            String fieldId = entry.getKey();
            Object rawValue = entry.getValue();

            // 获取字段类型，默认为 string
            String fieldType = fieldTypeMap.getOrDefault(fieldId, "string");

            // 转换值
            Object convertedValue = convertValueByType(rawValue, fieldType, "form_data." + fieldId);
            convertedData.put(fieldId, convertedValue);
        }

        return convertedData;
    }

    /**
     * 从模板 formContent 中解析字段类型定义
     * 返回 Map<字段ID, 字段类型>
     */
    private Map<String, String> parseFieldTypesFromTemplate(Template template) {
        Map<String, String> fieldTypes = new HashMap<>();

        if (StringUtils.isBlank(template.getFormContent())) {
            return fieldTypes;
        }

        try {
            // 解析 formContent: {"fields": [{"id": "weight", "type": "number", "label": "重量"}, ...]}
            Map<String, Object> formContent = objectMapper.readValue(
                    template.getFormContent(),
                    new TypeReference<Map<String, Object>>() {}
            );

            List<Map<String, Object>> fields = (List<Map<String, Object>>) formContent.get("fields");
            if (fields != null) {
                for (Map<String, Object> field : fields) {
                    String id = (String) field.get("id");
                    String type = (String) field.get("type");
                    if (StringUtils.isNotBlank(id)) {
                        fieldTypes.put(id, type != null ? type : "string");
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败时返回空 Map，后续使用默认 string 类型
            log.warn("解析模板表单字段类型失败: {}", e.getMessage());
        }

        return fieldTypes;
    }
}
