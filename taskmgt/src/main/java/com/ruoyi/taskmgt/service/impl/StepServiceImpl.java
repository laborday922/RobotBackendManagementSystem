package com.ruoyi.taskmgt.service.impl;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.vo.TaskStepVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StepServiceImpl implements IStepService {
    private final TaskRepository taskRepository;
    private final MessageSourceAccessor messageSourceAccessor;
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogService;
    private final RedisCache redisUtil;
    private final TaskReuseService taskReuseService;
    private final IRobotsService robotsService;

    /**
     * 创建步骤
     * @param taskId 任务Id
     * @return 步骤Vo列表
     */
    @Override
    public List<TaskStepVo> createSteps(Long taskId, List<TaskStep> steps) {
        Long tenantId = TenantContext.get();
        if(isAdmin(tenantId))tenantId=null;
        if(StringUtils.isEmpty(steps))return new ArrayList<>();
        //Assert.notEmpty(steps, "steps cannot be empty");
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        Long finalTenantId = tenantId;
        List<TaskStep> savedSteps = steps.stream()
                .map(step -> {
                    step.setTenantId(finalTenantId);
                    step.setTaskId(taskId);
                    step.setStatus(TaskStep.NOTSTART);
                    return this.stepRepository.insert(step);
                })
                .toList();

        return savedSteps.stream()
                .map(step -> {
                    TaskStepVo vo = CloneFactory.copy(new TaskStepVo(), step);
                    vo.setTaskName(task.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 修改步骤
     * @param taskId 任务Id
     * @param steps 修改的步骤
     */
    @Override
    public void updateSteps(Long taskId, List<TaskStep> steps) {
        Long tenantId = TenantContext.get();
        if(isAdmin(tenantId))tenantId=null;
        if (StringUtils.isEmpty(steps))return;
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });

        List<String> redisKeys = new ArrayList<>();
        for (TaskStep step : steps) {
            step.setTenantId(tenantId);
            if(step.getAssignedRobotId()!=null){
                if (Integer.valueOf(1).equals(task.getIsGroupTask()) && task.getRobotGroupId() != null){
                    Robot robot1 = new Robot();
                    robot1.setGroupId(task.getRobotGroupId());
                    List<Robot> robots = this.robotsService.selectRobotsList(robot1);
                    Robot robot2 = this.robotsService.selectRobotsById(step.getAssignedRobotId());
                    if(!robots.contains(robot2))throw new TaskmgtException(ReturnNo.INVALID_STEP_ROBOT_ASSIGNMENT,new String[]{"Step.name",step.getId().toString(),"步骤分配的执行机器人不属于任务机器人组"},this.messageSourceAccessor.getMessage(ReturnNo.INVALID_STEP_ROBOT_ASSIGNMENT.getMessage()));
                }
                else if (Integer.valueOf(0).equals(task.getIsGroupTask()) && task.getRobotId() != null){
                    if(!task.getRobotId().equals(step.getAssignedRobotId()))throw new TaskmgtException(ReturnNo.INVALID_STEP_ROBOT_ASSIGNMENT,new String[]{"Step.name",step.getId().toString(),"步骤分配的执行机器人与任务机器人不同"},this.messageSourceAccessor.getMessage(ReturnNo.INVALID_STEP_ROBOT_ASSIGNMENT.getMessage()));
                }
            }
            if(step.getAssignedRobotId()!=null&&this.stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(step.getAssignedRobotId(),null,List.of(TaskStep.EXECUTING,TaskStep.WAITING,TaskStep.WAITING_CALLBACK))!=0)
            {
                task.setStatus(Task.PENDING);
                task.setUpdateBy(SecurityUtils.getUsername());
                task.setUpdateTime(DateUtils.getNowDate());
                redisKeys.addAll(this.taskRepository.update(task));
            }
            TaskStep orginStep = this.stepRepository.findStepByTaskIdAndOrder(taskId,step.getOrderNum());
            if(StringUtils.isNotNull(orginStep)){
                step.setId(orginStep.getId());
                redisKeys.addAll(this.stepRepository.update(step));
            }
            else this.stepRepository.insert(step);
        }
        this.redisUtil.deleteObject(redisKeys);

    }

    /**
     * 获取步骤
     * @param taskId 任务Id
     * @return 步骤Vo列表
     */
    @Override
    public List<TaskStepVo> retrieveSteps(Long taskId) {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), taskId.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        List<TaskStep> taskSteps = this.stepRepository.findStepsByTaskId(taskId);
        return taskSteps.stream()
                .map(step -> {
                    TaskStepVo vo = CloneFactory.copy(new TaskStepVo(), step);
                    vo.setTaskName(task.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void completeStep(Long stepId) {
        TaskStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> {
                    String[] args = {messageSourceAccessor.getMessage("Step.name", LocaleContextHolder.getLocale()), stepId.toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
                            messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });

        if (!Objects.equals(step.getStatus(), TaskStep.EXECUTING)) {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Step.name", LocaleContextHolder.getLocale()), step.getId().toString(), step.getStatus().toString()};
            throw new TaskmgtException(ReturnNo.STATENOTALLOW, args, this.messageSourceAccessor.getMessage(ReturnNo.STATENOTALLOW.getMessage()));
        }

        // 标记当前步骤完成
        step.setStatus(TaskStep.FINISHED);
        step.setEndTime(new Date());
        List<String> redisKeys = stepRepository.update(step);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogService.record(step.getTaskId(), stepId, TaskLogEventType.STEP_COMPLETE,
                "步骤 " + step.getStepName() + " 完成" + "开始时间:" + step.getStartTime()+
                        "结束时间:" + step.getEndTime(), "system", null);

        // 查找下一个步骤
        List<TaskStep> steps = stepRepository.findStepsByTaskId(step.getTaskId());
        TaskStep nextStep = steps.stream()
                .filter(s -> s.getOrderNum() > step.getOrderNum())
                .min(Comparator.comparing(TaskStep::getOrderNum))
                .orElse(null);

        if (nextStep != null && Objects.equals(nextStep.getStatus(), TaskStep.NOTSTART)) {
            startStep(nextStep);
        } else {
            // 所有步骤完成，任务结束
            Task task = taskRepository.findById(step.getTaskId())
                    .orElseThrow(() -> {
                        String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), step.getTaskId().toString()};
                        return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args, this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                    });
            taskReuseService.completeTask(task);
        }
    }

    private void startStep(TaskStep step) {
        step.setStatus(TaskStep.EXECUTING);
        step.setStartTime(new Date());
        step.setUpdateBy("system");
        List<String> redisKeys = stepRepository.update(step);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogService.record(step.getTaskId(), step.getId(), TaskLogEventType.STEP_START,
                "步骤 " + step.getStepName() + " 开始执行", "system", null);
    }
}
