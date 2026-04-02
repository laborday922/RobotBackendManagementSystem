package com.ruoyi.taskmgt.service.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.app.domain.TAppConstraint;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.robots.service.IRobotGroupsService;
import com.ruoyi.taskmgt.domain.bo.Template;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CopyFrom({Template.class})
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("任务模板VO")
public class TemplateVo {
    private Long id;
    private String name;
    private String description;
    private List<Long> robotGroupIds;
    private Long appId;
    private String appName;
    //表单字段
    private String formContent;
    //标准工作流
    private String workflow;
    //0启用 1已禁用 2已删除
    private Byte status;
    private Date createTime;
    private Date updateTime;
    private List<String> robotGroupNames;
    private List<TAppConstraint> rules;
}
