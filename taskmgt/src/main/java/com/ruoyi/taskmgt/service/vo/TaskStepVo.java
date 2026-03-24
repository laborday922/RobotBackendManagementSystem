package com.ruoyi.taskmgt.service.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import io.swagger.annotations.ApiModel;
import lombok.*;

@CopyFrom({TaskStep.class})
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("任务步骤VO")
public class TaskStepVo {
    private Long id;

    private Long taskId;

    /** 所属任务名 */
    private String taskName;

    /** 步骤名称 */
    private String stepName;

    /** 具体描述 */
    private String description;

    /** 步骤序号 */
    private Integer orderNum;

    /** 步骤状态（0未开始 1进行中 2已完成 3已暂停 4已终止） */
    private Byte status;
}
