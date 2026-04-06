package com.ruoyi.mode.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 模式对象 sys_mode
 *
 * @author ruoyi
 */
@ApiModel(value = "SysMode", description = "模式实体类")
public class SysMode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模式ID */
    @ApiModelProperty(value = "模式ID", example = "1")
    @NotNull(message = "模式ID不能为空", groups = {UpdateGroup.class})
    private Long modeId;

    /** 模式名称 */
    @ApiModelProperty(value = "模式名称", required = true, example = "巡检模式")
    @NotBlank(message = "模式名称不能为空")
    @Size(min = 1, max = 50, message = "模式名称长度必须在1-50之间")
    private String modeName;

    /** 模式类型(system系统/custom自定义) */
    @ApiModelProperty(value = "模式类型", allowableValues = "system,custom", example = "system", notes = "system-系统模式，custom-自定义模式")
    @NotBlank(message = "模式类型不能为空")
    @Pattern(regexp = "^(system|custom)$", message = "模式类型只能是system或custom")
    private String modeType;

    /** 分类ID */
    @ApiModelProperty(value = "分类ID", example = "10")
    private Long categoryId;

    /** 模式颜色 */
    @ApiModelProperty(value = "模式颜色", example = "#FF5733", notes = "十六进制颜色代码")
    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "颜色格式错误，应为#加6位十六进制数")
    private String modeColor;

    /** 模式图标 */
    @ApiModelProperty(value = "模式图标", example = "fa fa-robot", notes = "FontAwesome图标类名或自定义图标标识")
    @Size(max = 100, message = "图标名称长度不能超过100")
    private String modeIcon;

    /** 模式描述 */
    @ApiModelProperty(value = "模式描述", example = "用于日常巡检工作的模式")
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

    /** 是否启用(0禁用 1启用) */
    @ApiModelProperty(value = "是否启用", allowableValues = "0,1", example = "1", notes = "0-禁用，1-启用")
    @Pattern(regexp = "^[01]$", message = "启用状态只能是0或1")
    private String enabled;

    /** 使用次数 */
    @ApiModelProperty(value = "使用次数", example = "128")
    @Min(value = 0, message = "使用次数不能为负数")
    private Long usageCount;

    /** 关联机器人数量 */
    @ApiModelProperty(value = "关联机器人数量", example = "5", notes = "使用该模式的机器人数量")
    private Long robotCount;

    /** 显示顺序 */
    @ApiModelProperty(value = "显示顺序", example = "1")
    @Min(value = 0, message = "显示顺序不能小于0")
    @Max(value = 999, message = "显示顺序不能大于999")
    private Integer orderNum;

    /** 删除标志（0代表存在 2代表删除） */
    @ApiModelProperty(value = "删除标志", allowableValues = "0,2", example = "0", notes = "0-存在，2-删除", hidden = true)
    private String delFlag;

    // 非数据库字段 - 分类名称
    @ApiModelProperty(value = "分类名称", example = "日常工作", notes = "关联的分类名称（非数据库字段）")
    private String categoryName;

    // 非数据库字段 - 参数列表（用于接收前端传递的参数）
    @ApiModelProperty(value = "参数列表", notes = "模式关联的参数配置列表（非数据库字段）")
    private List<SysModeParam> modeParams;

    // 分组校验接口
    public interface AddGroup {}
    public interface UpdateGroup {}

    // ==================== Getters and Setters ====================

    public Long getModeId() {
        return modeId;
    }

    public void setModeId(Long modeId) {
        this.modeId = modeId;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public String getModeType() {
        return modeType;
    }

    public void setModeType(String modeType) {
        this.modeType = modeType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getModeColor() {
        return modeColor;
    }

    public void setModeColor(String modeColor) {
        this.modeColor = modeColor;
    }

    public String getModeIcon() {
        return modeIcon;
    }

    public void setModeIcon(String modeIcon) {
        this.modeIcon = modeIcon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public Long getRobotCount() {
        return robotCount;
    }

    public void setRobotCount(Long robotCount) {
        this.robotCount = robotCount;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<SysModeParam> getModeParams() {
        return modeParams;
    }

    public void setModeParams(List<SysModeParam> modeParams) {
        this.modeParams = modeParams;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("modeId", getModeId())
                .append("modeName", getModeName())
                .append("modeType", getModeType())
                .append("categoryId", getCategoryId())
                .append("modeColor", getModeColor())
                .append("modeIcon", getModeIcon())
                .append("description", getDescription())
                .append("enabled", getEnabled())
                .append("usageCount", getUsageCount())
                .append("robotCount", getRobotCount())
                .append("orderNum", getOrderNum())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("modeParams", getModeParams())
                .toString();
    }
}