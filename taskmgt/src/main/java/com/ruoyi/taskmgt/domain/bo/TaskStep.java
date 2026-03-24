package com.ruoyi.taskmgt.domain.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.common.clonefactory.CopyNotNullTo;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.model.Stateful;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.mapper.po.TaskStepPo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

/**
 * 任务步骤执行记录对象 task_step
 */
@Data
@EqualsAndHashCode(callSuper = true)
@CopyFrom({TaskStepPo.class})
@CopyNotNullTo({TaskStepPo.class})
public class TaskStep extends BaseEntity implements Serializable, Stateful {
    private static final long serialVersionUID = 1L;

    /** 步骤ID */
    private Long id;

    /** 所属任务ID */
    private Long taskId;

    /** 步骤名称 */
    private String stepName;

    /** 步骤序号 */
    private Integer orderNum;

    /** 具体描述 */
    private String description;

    /** 步骤状态（0未开始 1进行中 2已完成 3已暂停 4已终止） */
    private Byte status;

    //任务状态
    public final static Byte NOTSTART = 0;
    public final static Byte EXECUTING = 1;
    public final static Byte FINISHED =2;
    public final static Byte PAUSED = 3;
    public final static Byte TERMINATED= 4;

    public static final Map<Byte, String> STATUSNAMES = new HashMap<>() {
        {
            put(NOTSTART, "Obj.NOTSTART");
            put(EXECUTING, "Obj.EXECUTING");
            put(PAUSED, "Obj.PAUSED");
            put(TERMINATED, "Obj.TERMINATED");
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
                    add(EXECUTING);
                    add(TERMINATED);
                }
            });

            put(EXECUTING, new HashSet<>() {
                {
                    add(PAUSED);
                    add(TERMINATED);
                    add(FINISHED);
                }
            });
            put(PAUSED, new HashSet<>() {
                {
                    add(EXECUTING);
                    add(TERMINATED);
                }
            });
            put(FINISHED, new HashSet<>() {
                {
                    add(NOTSTART);
                }
            });
        }
    };

    /** 执行日志 */
    private String log;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    @JsonIgnore
    @ToString.Exclude
    @Setter
    private TaskRepository taskRepository;

    @JsonIgnore
    @ToString.Exclude
    @Setter
    private StepRepository stepRepository;

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

    @Override
    @JsonIgnore
    public String getStatusName() {
        return STATUSNAMES.get(this.status);
    }
}
