package com.ruoyi.taskmgt.domain.bo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.common.clonefactory.CopyTo;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.taskmgt.mapper.po.TaskLogPo;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CopyFrom({TaskLogPo.class})
@CopyTo({TaskLogPo.class})
public class TaskLog extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;
    /** 任务ID */
    @NotNull
    private Long taskId;

    /** 步骤ID */
    private Long stepId;

    /** 事件类型 */
    @NotNull
    private String eventType;

    /** 内容 */
    private String content;

    /** 操作人 */
    private String operator;}