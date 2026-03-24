package com.ruoyi.taskmgt.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyTo;
import com.ruoyi.common.validation.NewGroup;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyTo({TaskStep.class})
@ApiModel("任务步骤DTO")
public class TaskStepDto {
    private Long id;

    /** 所属任务ID */
    private Long taskId;

    /** 步骤名称 */
    @NotBlank(message = "Dto.NotNull",groups = NewGroup.class)
    private String stepName;
    /** 具体描述 */
    private String description;

    private Long operationId;

    private String operationJson;

    /** 步骤序号 */
    @NotBlank(message = "Dto.NotNull",groups = NewGroup.class)
    private Integer orderNum;

    private String traceId;
    private String resultData;
    private String errorMsg;
    private Long assignedRobotId;
}
