package com.ruoyi.taskmgt.domain.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.common.clonefactory.CopyNotNullTo;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.model.Stateful;
import com.ruoyi.taskmgt.domain.TemplateRepository;
import com.ruoyi.taskmgt.mapper.po.TemplatePo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * 机器人任务模板对象 template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@CopyFrom({TemplatePo.class})
@CopyNotNullTo({TemplatePo.class})
public class Template extends BaseEntity  implements Serializable, Stateful {
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long id;

    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    private String name;

    /** 模板描述 */
    private String description;

    /** 关联的机器人组ID（可为空，表示不限制组） */
    private List<Long> robotGroupIds;

    /** 表单内容（JSON格式，定义任务参数） */
    private String formContent;

    /** 标准作业流程（JSON数组，定义步骤名称、顺序等） */
    private String workflow;

    /** 状态（0正常 1已禁用 2已删除） */
    private Byte status;

    @Setter
    @JsonIgnore
    @ToString.Exclude
    private TemplateRepository templateRepository;

    public final static Byte ENABLED = 0;
    public final static Byte DISABLED = 1;
    public final static Byte DELETED = 2;

    public static final Map<Byte, String> STATUSNAMES = new HashMap<>() {
        {
            put(ENABLED, "Obj.ENABLED");
            put(DISABLED, "Obj.DISABLED");
            put(DELETED, "Obj.DELETED");
        }
    };

    /**
     * 允许的状态迁移
     */
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>() {
        {
            put(ENABLED, new HashSet<>() {
                {
                    add(DISABLED);
                }
            });
            put(DISABLED, new HashSet<>() {
                {
                    add(ENABLED);
                    add(DELETED);
                }
            });
        }
    };

    /**
     * 是否允许状态迁移
     * @param status 迁移去的状态
     */
    @JsonIgnore
    @Override
    public boolean allowTransitStatus(@NotNull Byte status) {
        boolean ret = false;
        Set<Byte> allowStatusSet = toStatus.get(this.status);
        if (!Objects.isNull(allowStatusSet)) {
            ret = allowStatusSet.contains(status);
        }
        return ret;
    }

    /**
     * 获得当前状态名称
     */
    @Override
    @JsonIgnore
    public String getStatusName() {
        return STATUSNAMES.get(this.status);
    }
}
