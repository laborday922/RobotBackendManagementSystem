package com.ruoyi.taskmgt.service.impl;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.ITaskLogService;
import com.ruoyi.taskmgt.service.vo.TaskLogVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TaskLogServiceImpl implements ITaskLogService {

    private final TaskLogRepository taskLogRepository;
    private final MessageSourceAccessor messageSourceAccessor;
    private final TaskRepository taskRepository;
    private final StepRepository stepRepository;

    @Override
    public List<TaskLogVo> queryLogs(TaskLog query,String beginTime,String endTime) {
        Date beginTimeDate = new Date();
        Date endTimeDate = new Date();
        if(!StringUtils.isEmpty(beginTime)) beginTimeDate = DateUtils.parseDate(beginTime);
        if(!StringUtils.isEmpty(endTime)) endTimeDate = DateUtils.parseDate(endTime);
        List<TaskLog> logs = taskLogRepository.findTaskLogs(query,beginTimeDate,endTimeDate);
        PageHelper.clearPage();
        return logs.stream().map(taskLog -> {
            TaskLogVo vo = CloneFactory.copy(new TaskLogVo(), taskLog);
            Task task = this.taskRepository.findById(taskLog.getTaskId()).orElseThrow(()-> {
                String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), taskLog.getTaskId().toString()};
                return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
            });
            vo.setTaskName(task.getName());
            if(StringUtils.isNotNull(taskLog.getStepId())){
                TaskStep taskStep = this.stepRepository.findById(taskLog.getStepId()).orElseThrow(()-> {
                    String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), taskLog.getStepId().toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });
                vo.setStepName(taskStep.getStepName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TaskLogVo getLog(Long id) {
        TaskLog taskLog = taskLogRepository.findById(id)
                .orElseThrow(() -> {
                    String[] args = {messageSourceAccessor.getMessage("TaskLog.name", LocaleContextHolder.getLocale()), id.toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
                            messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });
        TaskLogVo vo = CloneFactory.copy(new TaskLogVo(), taskLog);
        Task task = this.taskRepository.findById(taskLog.getTaskId()).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        vo.setTaskName(task.getName());
        if(StringUtils.isNotNull(taskLog.getStepId())){
            TaskStep taskStep = this.stepRepository.findById(taskLog.getTaskId()).orElseThrow(()-> {
                String[] args = new String[]{this.messageSourceAccessor.getMessage("Task.name",LocaleContextHolder.getLocale()), id.toString()};
                return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
            });
            vo.setStepName(taskStep.getStepName());
        }
        return vo;
    }
}