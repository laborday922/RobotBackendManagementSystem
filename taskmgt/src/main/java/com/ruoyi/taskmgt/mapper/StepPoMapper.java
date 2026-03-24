package com.ruoyi.taskmgt.mapper;

import com.ruoyi.taskmgt.mapper.po.TaskStepPo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepPoMapper extends JpaRepository<TaskStepPo, Long> {
    List<TaskStepPo> findByTaskId(Long taskId);

    TaskStepPo findByTaskIdAndOrderNum(Long taskId, Integer orderNum);
}
