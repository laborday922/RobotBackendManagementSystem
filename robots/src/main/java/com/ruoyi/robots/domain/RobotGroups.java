package com.ruoyi.robots.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;

/**
 * 机器人分组对象 robot_groups
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class RobotGroups extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分组ID */
    private Long id;

    /** 分组名称 */
    @NotBlank(message = "分组名称不能为空")
    @Excel(name = "分组名称")
    private String name;

    /** 分组描述 */
    @NotBlank(message = "分组描述不能为空")
    @Excel(name = "分组描述")
    private String description;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

   private Long tenantId;
}
