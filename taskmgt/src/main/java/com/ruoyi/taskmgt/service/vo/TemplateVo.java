package com.ruoyi.taskmgt.service.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String formContent;
    private String workflow;
    private Byte status;
    private Date createTime;
    private Date updateTime;
    private List<String> robotGroupNames;
    private IRobotGroupsService robotGroupsService;
    public void setRobotGroupNames(List<Long> robotGroupIds){
        List<String> robotGroupNames = new ArrayList<>();
        for(Long robotGroupId : robotGroupIds){
            robotGroupNames.add(this.robotGroupsService.selectRobotGroupsById(robotGroupId).getName());
        }
        this.robotGroupNames=robotGroupNames;
    }
}
