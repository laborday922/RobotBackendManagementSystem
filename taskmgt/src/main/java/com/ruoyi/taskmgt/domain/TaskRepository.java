package com.ruoyi.taskmgt.domain;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.mapper.TaskPoMapper;
import com.ruoyi.taskmgt.mapper.po.TaskPo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TaskRepository {
    private final TaskPoMapper taskPoMapper;
    private final RedisCache redisUtil;
    private final MessageSourceAccessor messageSourceAccessor;
    private final static String TASKBYNAME = "TN_%s";
    private final static String TASKBYID = "T%d";

    public TaskRepository(TaskPoMapper taskPoMapper, RedisCache redisUtil, MessageSourceAccessor messageSourceAccessor) {
        this.taskPoMapper = taskPoMapper;
        this.redisUtil = redisUtil;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     * 构造满血的Task对象
     *
     * @param bo 充血的task对象
     * @return 满血的Task对象
     */
    private Task build(Task bo) {
        if (Objects.nonNull(bo)){
            bo.setTaskRepository(this);
        }
        return bo;
    }

    /**
     * 构成满血的Task对象
     *
     * @param po       Task Po 对象
     * @param redisKey redis key
     * @return 满血的Task对象
     */
    private Task build(TaskPo po, Optional<String> redisKey) {
        if (Objects.nonNull(po)) {
            Task bo = CloneFactory.copy(new Task(), po);
            redisKey.ifPresent(key -> this.redisUtil.setCacheObject(key, bo));
            return this.build(bo);
        }
        return null;
    }

    /**
     * 以id找对象
     *
     * @param id 对象id
     * @return task对象
     */
    public Optional<Task> findById(Long id) {
        Assert.notNull(id, "TaskRepository.findById: TaskRepository.findById: id is null");
        String key = String.format(TASKBYID, id);
        Task bo = this.redisUtil.getCacheObject(key);
        if (Objects.isNull(bo)) {
            return this.taskPoMapper.findById(id).map(po -> this.build(po, Optional.of(key)));
        } else {
            this.build(bo);
            return Optional.of(bo);
        }
    }

    /**
     * 用用户名查找对象
     *
     * @param name 任务名
     * @return
     */
    public Optional<Task> findByName(String name) {
        Assert.hasText(name, "TaskRepository.findByName: TaskRepository.findByName: name is empty");
        log.debug("findByName: name={}", name);
        String key = String.format(TASKBYNAME, name);
        Task task = (Task) this.redisUtil.getCacheObject(key);
        log.debug("findByName: redis taskcore={}", task);
        if (Objects.isNull(task)) {
            return this.taskPoMapper.findByName(name)
                    .filter(po -> po.getName().equals(name))
                    .map(po -> this.build(po, Optional.of(key)));
        } else {
            return Optional.of(this.build(task));
        }
    }

    public Task insert(Task task) {
        Assert.notNull(task, "TaskRepository.insert: taskcore can not be null.");
        task.setId(null);
        TaskPo taskPo = CloneFactory.copyNotNull(new TaskPo(), task);
        taskPo.setCreateTime(new Date());
        taskPo.setCreateBy(SecurityUtils.getUsername());
        try {
            TaskPo savedPo = this.taskPoMapper.save(taskPo);
            return CloneFactory.copy(new Task(), savedPo);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            log.debug("taskRepository:insert: msg={}", msg);
            if (msg != null && msg.contains("task_name_index")) {
                String[] args = {this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), task.getName()};
                throw new TaskmgtException(ReturnNo.SAMEOBJECT, args, this.messageSourceAccessor.getMessage(ReturnNo.SAMEOBJECT.getMessage()));
            }
            throw e;
        }
    }

    public List<String> update(Task task) {
        Assert.notNull(task, "TaskRepository.updatetask: taskcore can not be null.");
        Assert.notNull(task.getId(), "TaskRepository.update: taskcore id can not be null.");

        TaskPo oldtaskPo = this.taskPoMapper.findById(task.getId()).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), task.getId().toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        TaskPo newPo = CloneFactory.copyNotNull(oldtaskPo, task);
        newPo.setUpdateTime(new Date());
        try {
            this.taskPoMapper.save(newPo);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            log.debug("taskRepository:update: msg={}", msg);
            if (msg != null && msg.contains("task_name_index")) {
                String[] args = {this.messageSourceAccessor.getMessage("Task.name", LocaleContextHolder.getLocale()), task.getName()};
                throw new TaskmgtException(ReturnNo.SAMEOBJECT, args, this.messageSourceAccessor.getMessage(ReturnNo.SAMEOBJECT.getMessage()));
            }
            throw e;
        }
        String keyId = String.format(TASKBYID, task.getId());
        String keyName = String.format(TASKBYNAME, oldtaskPo.getName());
        List<String> keys = new ArrayList<>();
        keys.add(keyId);
        keys.add(keyName);
        return keys;
    }

    public List<Task> getTasks(Byte status, Integer isGroupTask, String name, Long robotId, Long robotGroupId, Integer taskType, Integer riskLevel, Long templateId, Long tenantId) {
        Specification<TaskPo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), name + "%"));
            }
            if (isGroupTask!=null) {
                predicates.add(cb.equal(root.get("isGroupTask"), isGroupTask));
            }
            if (robotId!=null) {
                predicates.add(cb.equal(root.get("robotId"), robotId));
            }
            if (robotGroupId!=null) {
                predicates.add(cb.equal(root.get("robotGroupId"), robotGroupId));
            }
            if (taskType != null) {
                predicates.add(cb.equal(root.get("taskType"), taskType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (riskLevel != null) {
                predicates.add(cb.equal(root.get("riskLevel"), riskLevel));
            }
            if (templateId != null) {
                predicates.add(cb.equal(root.get("templateId"), templateId));
            }
            predicates.add(cb.notEqual(root.get("status"), Task.DELETED));
            query.orderBy(cb.asc(root.get("status")),
                    cb.asc(root.get("globalPendingOrder")),
                    cb.asc(root.get("pendingOrder")),
                    cb.desc(root.get("priority")),
                    cb.asc(root.get("createTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<TaskPo> taskPos = taskPoMapper.findAll(spec);
        return taskPos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }

    public List<Task> findByRobotIdAndStatus(Long robotId, Byte status) {
        List<TaskPo> taskPos = this.taskPoMapper.findByRobotIdAndStatus(robotId,status);
        return taskPos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByRobotIds(Byte status, Integer isGroupTask, String name, List<Long> robotIds, Long robotGroupId, Integer taskType, Integer riskLevel, Long templateId, Long tenantId) {
        Sort sort = Sort.by(
                Sort.Order.asc("status"),
                Sort.Order.asc("pendingOrder"),
                Sort.Order.desc("priority"),
                Sort.Order.desc("createTime")
        );
        Specification<TaskPo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), name + "%"));
            }
            if (isGroupTask!=null) {
                predicates.add(cb.equal(root.get("isGroupTask"), isGroupTask));
            }
            if (robotIds!=null) {
                predicates.add(root.get("robotId").in(robotIds));
            }
            if (robotGroupId!=null) {
                predicates.add(cb.equal(root.get("robotGroupId"), robotGroupId));
            }
            if (taskType != null) {
                predicates.add(cb.equal(root.get("taskType"), taskType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (riskLevel != null) {
                predicates.add(cb.equal(root.get("riskLevel"), riskLevel));
            }
            if (templateId != null) {
                predicates.add(cb.equal(root.get("templateId"), templateId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<TaskPo> taskPos = taskPoMapper.findAll(spec,sort);
        return taskPos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }
}
