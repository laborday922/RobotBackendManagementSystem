package com.ruoyi.data.dashboard.controller.vo;

import lombok.Data;

import java.util.Date;


@Data
public class TaskExecutionItem {

    private Long id;

    private String name;

    private Long robotId;

    private Integer status;

    private String statusDesc;

    private Date scheduledTime;

    private Integer priority;

}