package com.ruoyi.taskmgt.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.clonefactory.CopyTo;
import com.ruoyi.common.validation.NewGroup;
import com.ruoyi.taskmgt.domain.bo.Template;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyTo({Template.class})
@ApiModel("任务模板DTO")
public class TemplateDto {
    private Long id;
    @NotBlank(message = "Dto.NotNull",groups = NewGroup.class)
    private String name;
    private String description;
    private List<Long> robotGroupIds;
    private String formContent;
    private String workflow;
}