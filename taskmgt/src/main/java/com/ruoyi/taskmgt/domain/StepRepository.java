package com.ruoyi.taskmgt.domain;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.mapper.StepPoMapper;
import com.ruoyi.taskmgt.mapper.po.TaskPo;
import com.ruoyi.taskmgt.mapper.po.TaskStepPo;
import com.ruoyi.taskmgt.service.impl.TaskLogReuseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class StepRepository {
    private final StepPoMapper stepPoMapper;
    private final TaskRepository taskRepository;
    private final RedisCache redisUtil;
    private final MessageSourceAccessor messageSourceAccessor;
    private final static String STEPBYNAME = "SN_%s";
    private final static String STEPBYID = "S%d";
    private final TaskLogReuseService taskLogService;

    public StepRepository(StepPoMapper stepPoMapper, TaskRepository taskRepository, RedisCache redisUtil, MessageSourceAccessor messageSourceAccessor, TaskLogReuseService taskLogService) {
        this.stepPoMapper = stepPoMapper;
        this.taskRepository = taskRepository;
        this.redisUtil = redisUtil;
        this.messageSourceAccessor = messageSourceAccessor;
        this.taskLogService = taskLogService;
    }

    /**
     * 构造满血的TaskStep对象
     *
     * @param bo 充血的taskStep对象
     * @return 满血的TaskStep对象
     */
    private TaskStep build(TaskStep bo) {
        if (Objects.nonNull(bo)){
            bo.setStepRepository(this);
            bo.setTaskRepository(this.taskRepository);
        }
        return bo;
    }

    /**
     * 构成满血的TaskStep对象
     *
     * @param po       TaskStep Po 对象
     * @param redisKey redis key
     * @return 满血的TaskStep对象
     */
    private TaskStep build(TaskStepPo po, Optional<String> redisKey) {
        if (Objects.nonNull(po)) {
            TaskStep bo = CloneFactory.copy(new TaskStep(), po);
            redisKey.ifPresent(key -> this.redisUtil.setCacheObject(key, bo));
            return this.build(bo);
        }
        return null;
    }

    /**
     * 以id找对象
     *
     * @param id 对象id
     * @return taskStep对象
     */
    public Optional<TaskStep> findById(Long id) {
        Assert.notNull(id, "TaskRepository.findById: TaskRepository.findById: id is null");
        String key = String.format(STEPBYID, id);
        TaskStep bo = (TaskStep) this.redisUtil.getCacheObject(key);
        if (Objects.isNull(bo)) {
            return this.stepPoMapper.findById(id).map(po -> this.build(po, Optional.of(key)));
        } else {
            this.build(bo);
            return Optional.of(bo);
        }
    }

    public List<TaskStep> findStepsByTaskId(Long taskId) {
        List<TaskStepPo>taskStepPos = this.stepPoMapper.findByTaskIdOrderByOrderNumAsc(taskId);
        return taskStepPos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }

    public TaskStep findStepByTaskIdAndOrder(Long taskId,Integer orderNum) {
        TaskStepPo taskStepPo = this.stepPoMapper.findByTaskIdAndOrderNum(taskId,orderNum);
        if(StringUtils.isNotNull(taskStepPo))return CloneFactory.copy(new TaskStep(), taskStepPo);
        else return null;
    }

    public List<String> delete(Long id) {
        Assert.notNull(id, "StepRepository.delete: id must not be null");
        String key = String.format(STEPBYID, id);
        this.stepPoMapper.deleteById(id);
        return List.of(key);
    }

    /**
     * @param taskId 任务id
     * @return
     * &#064;description  删除任务的所有步骤
     **/
    public Set<String> deleteStepsByTaskId(Long taskId) {
        Assert.notNull(taskId, "TaskRepository.deleteTaskAllSteps: id is null");
        List<TaskStep> taskSteps = this.findStepsByTaskId(taskId);
        if (StringUtils.isEmpty(taskSteps)) {
            return Set.of();
        }
        Set<Long> stepIds = new HashSet<>();
        for (TaskStep step : taskSteps) {
            stepIds.add(step.getId());
        }
        Set<String> allRedisKeys = new HashSet<>();
        for (Long stepId : stepIds) {
            allRedisKeys.addAll(this.delete(stepId));
        }
        return allRedisKeys;
    }

    public List<String> update(TaskStep step) {
        Assert.notNull(step, "StepRepository.updatestep: step can not be null.");
        Assert.notNull(step.getId(), "StepRepository.update: step id can not be null.");

        TaskStepPo oldstepPo = this.stepPoMapper.findById(step.getId()).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Step.name",LocaleContextHolder.getLocale()), step.getId().toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        TaskStepPo newPo = CloneFactory.copyNotNull(oldstepPo, step);
        if(Objects.equals(newPo.getTraceId(), "null"))newPo.setTraceId(null);
        newPo.setUpdateTime(new Date());
        newPo.setUpdateBy(getCurrentUsername());
        try {
            this.stepPoMapper.save(newPo);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            log.debug("stepRepository:update: msg={}", msg);
            if (msg != null && msg.contains("name_index")) {
                String[] args = {this.messageSourceAccessor.getMessage("Step.name", LocaleContextHolder.getLocale()), step.getStepName()};
                throw new TaskmgtException(ReturnNo.SAMEOBJECT, args, this.messageSourceAccessor.getMessage(ReturnNo.SAMEOBJECT.getMessage()));
            }
            throw e;
        }
        String keyId = String.format(STEPBYID, step.getId());
        String keyName = String.format(STEPBYNAME, oldstepPo.getStepName());
        List<String>key = new ArrayList<>();
        key.add(keyId);
        key.add(keyName);
        return key;
    }

    public TaskStep insert(TaskStep step) {
        Assert.notNull(step, "StepRepository.insert: step can not be null.");
        step.setId(null);
        TaskStepPo stepPo = CloneFactory.copyNotNull(new TaskStepPo(), step);
        stepPo.setCreateTime(new Date());
        stepPo.setCreateBy(SecurityUtils.getUsername());
        try {
            TaskStepPo savedPo = this.stepPoMapper.save(stepPo);
            return CloneFactory.copy(new TaskStep(), savedPo);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            log.debug("stepRepository:insert: msg={}", msg);
            if (msg != null && msg.contains("task_step_name")) {
                String[] args = {messageSourceAccessor.getMessage("TaskStep.stepName", LocaleContextHolder.getLocale()), step.getStepName()};
                throw new TaskmgtException(ReturnNo.SAMEOBJECT, args, messageSourceAccessor.getMessage(ReturnNo.SAMEOBJECT.getMessage()));
            }
            throw e;
        }
    }
    private String getCurrentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    public TaskStep findByTraceId(String traceId) {
        TaskStepPo taskStepPo = this.stepPoMapper.findByTraceId(traceId);
        if(StringUtils.isNotNull(taskStepPo))return CloneFactory.copy(new TaskStep(), taskStepPo);
        else return null;
    }

    public List<TaskStep> getSteps(Byte status, String name, Long assignedRobotId) {
        Specification<TaskStepPo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), name + "%"));
            }
            if (assignedRobotId!=null) {
                predicates.add(cb.equal(root.get("assignedRobotId"), assignedRobotId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            predicates.add(cb.notEqual(root.get("status"), Task.DELETED));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<TaskStepPo> stepPos = stepPoMapper.findAll(spec);
        return stepPos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }

    public long countByAssignedRobotIdAndTaskIdAndStatusIn(Long assignedRobotId, Long taskId,List<Byte> status) {
        Specification<TaskStepPo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (assignedRobotId!=null) {
                predicates.add(cb.equal(root.get("assignedRobotId"), assignedRobotId));
            }
            if (taskId!=null){
                predicates.add(cb.equal(root.get("taskId"),taskId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(root.get("status").in(status));
            }

            predicates.add(cb.notEqual(root.get("status"), Task.DELETED));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return stepPoMapper.findAll(spec).size();
    }
}
