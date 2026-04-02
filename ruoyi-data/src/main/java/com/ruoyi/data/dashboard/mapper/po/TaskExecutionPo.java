package com.ruoyi.data.dashboard.mapper.po;

import lombok.Data;

import java.util.Date;

@Data
public class TaskExecutionPo {

    private Long id;

    private String name;

    private Long robotId;

    private Integer status;

    private Date scheduledTime;

    private Integer priority;

}