package com.ruoyi.taskmgt.mapper.po;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * 任务持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "tm_task", uniqueConstraints = {
        @UniqueConstraint(name = "task_name_index", columnNames = "name")
})
@Entity
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskPo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 任务名称 */
    private String name;

    /** 优先级（1-10） */
    private Integer priority;

    /** 是否组任务（1是 0否） */
    private Integer isGroupTask;

    /** 具体描述 */
    private String description;

    /** 任务时长（分钟） */
    private Integer duration;

    /** 任务类型（1定时 2电量 3闲时） */
    private Integer taskType;

    /** 任务状态（0执行中 1准备中 2已暂停 3未开始 4已禁用 5已终止 6已结束 7已删除） */
    private Byte status;

    /** 风险等级（0正常 1风险 2高风险） */
    private Integer riskLevel;

    /** Cron表达式（定时任务） */
    private String cronExpression;

    /** 电量阈值（电量任务） */
    private Integer batteryThreshold;

    /** 闲时等待时间（分钟） */
    private Integer idleTime;

    /** 指定机器人ID */
    private Long robotId;

    /** 指定机器人组ID */
    private Long robotGroupId;

    /** 定时开始时间 */
    private Date scheduledTime;

    /** 暂停断点信息 */
    private String pauseSnapshot;

    /** 终止原因 */
    private String terminateReason;

    private Integer pendingOrder;

    @Column(columnDefinition = "text")
    private String formContent;
}
