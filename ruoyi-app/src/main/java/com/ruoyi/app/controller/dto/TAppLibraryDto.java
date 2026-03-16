package com.ruoyi.app.controller.dto;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class TAppLibraryDto
{


    /** 主键，自增ID */
    private Long id;

    /** 应用唯一标识（业务ID） */
    private String appId;

    /** 应用名称 */
    private String appName;

    /** 应用类型：0(交互类)、1(控制类)、2(监控类) */
    private Long appType;


    /** 应用描述 */
    private String description;

    /** 应用版本号（如1.0.0） */
    private String version;


}
