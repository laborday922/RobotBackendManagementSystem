package com.ruoyi.taskmgt.service.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.taskmgt.domain.bo.Task;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.util.Date;

@CopyFrom({Task.class})
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("任务VO")
public class TaskVo {
    /** 任务ID */
    private Long id;

    @Setter
    /** 模板名称（关联查询） */
    private String templateName;

    /** 任务名称 */
    private String name;

    /** 优先级 */
    private Integer priority;

    /** 是否组任务（1/0） */
    private Integer isGroupTask;

    /** 任务时长（分钟） */
    private Integer duration;

    /** 任务类型 */
    private Integer taskType;

    /** 任务状态 */
    private Byte status;

    /** 风险等级 */
    private Integer riskLevel;

    /** Cron表达式 */
    private String cronExpression;

    /** 电量阈值 */
    private Integer batteryThreshold;

    /** 闲时等待时间 */
    private Integer idleTime;

    /** 指定机器人名称 */
    private String robotName;

    /** 指定机器人组名称 */
    private String robotGroupName;

    /** 定时开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduledTime;

    /** 暂停断点信息 */
    private String pauseSnapshot;

    /** 终止原因 */
    private String terminateReason;

    private String formContent;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}