package com.ruoyi.taskmgt.mapper.po;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "tm_task_log")
@Entity
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskLogPo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    /** 任务ID */
    @Column(nullable = false)
    private Long taskId;

    /** 步骤ID（可为空） */
    private Long stepId;

    /** 事件类型 */
    @Column(length = 50, nullable = false)
    private String eventType;

    /** 事件描述 */
    @Column(columnDefinition = "text")
    private String content;

    /** 操作人 */
    @Column(length = 50)
    private String operator;

}