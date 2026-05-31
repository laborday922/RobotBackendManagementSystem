package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 模式参数对象 sys_mode_param
 *
 * @author ruoyi
 */
@Getter
@Setter
public class SysModeParam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 参数ID */
    private Long paramId;

    /** 所属模式ID */
    private Long modeId;

    /** 参数名称 */
    private String paramName;

    /** 参数类型(string文本/number数字/boolean开关/select下拉/range滑块) */
    private String paramType;

    /** 参数描述 */
    private String paramDescription;

    /** 参数默认值 */
    private String paramValue;

    /** 选项配置(JSON格式) */
    private String paramOptions;

    /** 最小值 */
    private Long paramMin;

    /** 最大值 */
    private Long paramMax;

    /** 单位 */
    private String paramUnit;

    /** 显示顺序 */
    private Integer orderNum;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 租户ID */
    private Long tenantId;

    // 非数据库字段 - 模式名称
    private String modeName;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("paramId", getParamId())
                .append("modeId", getModeId())
                .append("paramName", getParamName())
                .append("paramType", getParamType())
                .append("paramDescription", getParamDescription())
                .append("paramValue", getParamValue())
                .append("paramOptions", getParamOptions())
                .append("paramMin", getParamMin())
                .append("paramMax", getParamMax())
                .append("paramUnit", getParamUnit())
                .append("orderNum", getOrderNum())
                .append("delFlag", getDelFlag())
                .append("tenantId", getTenantId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
