package com.ruoyi.taskmgt.domain;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import com.ruoyi.taskmgt.mapper.TaskLogPoMapper;
import com.ruoyi.taskmgt.mapper.po.TaskLogPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TaskLogRepository {
    private final TaskLogPoMapper taskLogPoMapper;
    private final RedisCache redisUtil;

    private static final String TASK_LOG_BY_ID = "TL%d";

    /**
     * 将 PO 转换为 BO，并缓存
     */
    private TaskLog build(TaskLogPo po, Optional<String> redisKey) {
        if (Objects.nonNull(po)) {
            TaskLog bo = CloneFactory.copy(new TaskLog(), po);
            redisKey.ifPresent(key -> redisUtil.setCacheObject(key, bo));
            return bo;
        }
        return null;
    }

    /**
     * 插入日志
     */
    public TaskLog insert(TaskLog taskLog) {
        Assert.notNull(taskLog, "TaskLogRepository.insert: taskLog must not be null");
        taskLog.setId(null);
        TaskLogPo po = CloneFactory.copy(new TaskLogPo(), taskLog);
        po.setCreateTime(new Date());
        TaskLogPo saved = taskLogPoMapper.save(po);
        return CloneFactory.copy(new TaskLog(), saved);
    }

    /**
     * 根据 ID 查询（带缓存）
     */
    public Optional<TaskLog> findById(Long id) {
        Assert.notNull(id, "TaskLogRepository.findById: id is null");
        String key = String.format(TASK_LOG_BY_ID, id);
        TaskLog cached = redisUtil.getCacheObject(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        return taskLogPoMapper.findById(id).map(po -> build(po, Optional.of(key)));
    }

    public List<TaskLog> findTaskLogs(TaskLog query, Date beginTime, Date endTime, Long tenantId) {
        Specification<TaskLogPo> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getTaskId() != null) {
                predicates.add(cb.equal(root.get("taskId"), query.getTaskId()));
            }
            if (query.getStepId() != null) {
                predicates.add(cb.equal(root.get("stepId"), query.getStepId()));
            }
            if (query.getEventType() != null && !query.getEventType().isEmpty()) {
                predicates.add(cb.equal(root.get("eventType"), query.getEventType()));
            }
            if (query.getOperator() != null && !query.getOperator().isEmpty()) {
                predicates.add(cb.like(root.get("operator"), "%" + query.getOperator() + "%"));
            }
            if (beginTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), beginTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<TaskLogPo> taskLogPos = taskLogPoMapper.findAll(spec);
        return taskLogPos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }

    /**
     * 根据任务 ID 删除所有日志
     */
    public void deleteByTaskId(Long taskId) {
        Assert.notNull(taskId, "TaskLogRepository.deleteByTaskId: taskId is null");
        List<TaskLogPo> logs = taskLogPoMapper.findAll((root, query, cb) ->
                cb.equal(root.get("taskId"), taskId));
        taskLogPoMapper.deleteAll(logs);
        // 删除缓存（如果需要）
        logs.forEach(log -> redisUtil.deleteObject(String.format(TASK_LOG_BY_ID, log.getId())));
    }
}