package com.ruoyi.taskmgt.mapper;

import com.ruoyi.taskmgt.mapper.po.TaskStepPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StepPoMapper extends JpaRepository<TaskStepPo, Long>, JpaSpecificationExecutor<TaskStepPo> {
    List<TaskStepPo> findByTaskIdOrderByOrderNumAsc(Long taskId);

    TaskStepPo findByTaskIdAndOrderNum(Long taskId, Integer orderNum);

    TaskStepPo findByTraceId(String traceId);

}
