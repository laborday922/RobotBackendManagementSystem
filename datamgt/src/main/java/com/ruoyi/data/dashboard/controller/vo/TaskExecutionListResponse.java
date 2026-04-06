package com.ruoyi.data.dashboard.controller.vo;

import lombok.Data;

import java.util.List;

@Data
public class TaskExecutionListResponse {

    private  List<TaskExecutionItem> runningTasks;

    private List<TaskExecutionItem> pendingTasks;

    private Integer total;

}