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

    /** 总步骤数 */
    private Integer totalSteps;

    /** 已完成步骤数 */
    private Integer completedSteps;

    /** 进度百分比 0-100 */
    private Integer progress;

}