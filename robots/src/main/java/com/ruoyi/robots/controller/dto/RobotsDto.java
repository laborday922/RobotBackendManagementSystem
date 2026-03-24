package com.ruoyi.robots.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class RobotsDto {
    /** 机器人ID */
    private Long id;
    @NotBlank(message = "机器人编号不能为空")
    /** 机器人编号 */
    private String code;
    /** 机器人名称 */
    @NotBlank(message = "机器人名称不能为空")
    @Excel(name = "机器人名称")
    private String name;

    /** 所属分组ID（逻辑外键） */
    @NotNull(message = "所属分组不能为空")
    @Excel(name = "所属分组ID", readConverterExp = "逻=辑外键")
    private Long groupId;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    @NotBlank(message = "生产厂家不能为空")
    private String manufacturer;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 所属区域 */
    @Excel(name = "所属区域")
    @NotBlank(message = "所属区域不能为空")
    private String area;


}
