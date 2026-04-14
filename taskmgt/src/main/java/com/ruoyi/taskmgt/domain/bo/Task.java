package com.ruoyi.taskmgt.domain.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.common.clonefactory.CopyNotNullTo;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.model.Stateful;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.mapper.po.TaskPo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.*;

/**
 * 机器人任务对象 task
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CopyFrom({TaskPo.class})
@CopyNotNullTo({TaskPo.class})
public class Task extends BaseEntity implements Serializable, Stateful {
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 任务名称 */
    @NotBlank(message = "任务名称不能为空")
    private String name;

    /** 优先级（1-10，数值越小优先级越高） */
    private Integer priority;

    /** 是否组任务（1是 0否） */
    private Integer isGroupTask;

    /** 任务时长（分钟） */
    private Integer duration;

    /** 任务类型（1定时 2电量 3闲时） */
    private Integer taskType;

    /** 任务状态（ 0执行中 1准备中 2已暂停 3未开始 4已禁用 5已终止 6已结束 7已删除 ） */
    private Byte status;

    private Long tenantId;
    //任务状态
    public final static Byte EXECUTING = 0;
    public final static Byte PENDING = 1;
    public final static Byte PAUSED = 2;
    public final static Byte NOTSTART = 3;
    public final static Byte DISABLED= 4;
    public final static Byte TERMINATED= 5;
    public final static Byte FINISHED =6;
    public final static Byte DELETED =7;

    public static final Map<Byte, String> STATUSNAMES = new HashMap<>() {
        {
            put(NOTSTART, "Obj.NOTSTART");
            put(PENDING, "Obj.PENDING");
            put(EXECUTING, "Obj.EXECUTING");
            put(PAUSED, "Obj.PAUSED");
            put(DISABLED, "Obj.DISABLED");
            put(TERMINATED, "Obj.TERMINATED");
            put(DELETED, "Obj.DELETED");
            put(FINISHED, "Obj.FINISHED");
        }
    };

    /**
     * 允许的状态迁移
     */
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>() {
        {
            put(NOTSTART, new HashSet<>() {
                {
                    add(DISABLED);
                    add(PENDING);
                }
            });
            put(DISABLED, new HashSet<>() {
                {
                    add(NOTSTART);
                    add(DELETED);
                }
            });
            put(PENDING, new HashSet<>() {
                {
                    add(NOTSTART);
                    add(EXECUTING);
                }
            });
            put(EXECUTING, new HashSet<>() {
                {
                    add(PAUSED);
                    add(TERMINATED);
                    add(FINISHED);
                    add(NOTSTART);

                }
            });
            put(PAUSED, new HashSet<>() {
                {
                    add(EXECUTING);
                    add(PENDING);
                    add(TERMINATED);
                }
            });
            put(FINISHED, new HashSet<>(){
                {
                    add(NOTSTART);
                }
            });
        }
    };

    /** 具体描述 */
    private String description;

    /** 风险等级（0正常 1风险 2高风险） */
    private Integer riskLevel;

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

    /** 暂停时的断点信息 */
    private String pauseSnapshot;

    /** 终止原因 */
    private String terminateReason;

    /**准备队列顺序*/
    private Integer pendingOrder;

    private Integer globalPendingOrder;
    private String formContent;

    @JsonIgnore
    @ToString.Exclude
    @Setter
    private TaskRepository taskRepository;
    @Override
    @JsonIgnore
    public boolean allowTransitStatus(Byte status) {
        boolean ret = false;
        Set<Byte> allowStatusSet = toStatus.get(this.status);
        if (!Objects.isNull(allowStatusSet)) {
            ret = allowStatusSet.contains(status);
        }
        return ret;
    }

    /**
     * 获得当前状态名称
     */
    @Override
    @JsonIgnore
    public String getStatusName() {
        return STATUSNAMES.get(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
