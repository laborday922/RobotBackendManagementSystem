package com.ruoyi.taskmgt.mapper.po;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name= "tm_task_step")
@Entity
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskStepPo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
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

    /** 执行日志 */
    @Column(columnDefinition = "text")
    private String log;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    private Long operationId;

    @Column(columnDefinition = "text")
    private String operationJson;

    private String traceId;
    private String resultData;
    private String errorMsg;
    private Long assignedRobotId;
}
