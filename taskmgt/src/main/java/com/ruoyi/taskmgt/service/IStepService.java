package com.ruoyi.taskmgt.service;


import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.vo.TaskStepVo;

import java.util.List;

public interface IStepService {
    List<TaskStepVo> createSteps(Long taskId, List<TaskStep> steps);

    void completeStep(Long id);

    void updateSteps(Long id, List<TaskStep> steps);

    List<TaskStepVo> retrieveSteps(Long id);
}
