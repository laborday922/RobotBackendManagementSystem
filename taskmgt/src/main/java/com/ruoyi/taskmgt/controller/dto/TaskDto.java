package com.ruoyi.taskmgt.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyTo;
import com.ruoyi.common.validation.NewGroup;
import com.ruoyi.taskmgt.domain.bo.Task;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyTo({Task.class})
@ApiModel("任务DTO")
public class TaskDto {
    /** 任务ID */
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 具体描述 */
    private String description;

    /** 任务名称 */
    @NotBlank(message = "Dto.NotNull",groups = NewGroup.class)
    private String name;

    /** 优先级（1-10，数值越小优先级越高） */
    private Integer priority;

    /** 是否组任务（1是 0否） */
    private Integer isGroupTask;

    /** 任务时长（分钟） */
    private Integer duration;

    /** 任务类型（1定时 2电量 3闲时） */
    private Integer taskType;

    /** Cron表达式（定时任务时填写） */
    private String cronExpression;

    /** 电量阈值（电量任务时填写，如 80） */
    private Integer batteryThreshold;

    /** 闲时等待时间（分钟，闲时任务时填写） */
    private Integer idleTime;

    /** 指定机器人ID（非组任务时） */
    private Long robotId;

    /** 指定机器人组ID（组任务时） */
    private Long robotGroupId;

    /** 任务开始时间（定时任务时使用） */
    private Date scheduledTime;

    /** 资源内任务准备顺序  */
    private Integer pendingOrder;

    /** 全局任务准备顺序 */
    private Integer globalPendingOrder;

    /** 终止任务原因  */
    private String terminateReason;

    private String formContent;
}
