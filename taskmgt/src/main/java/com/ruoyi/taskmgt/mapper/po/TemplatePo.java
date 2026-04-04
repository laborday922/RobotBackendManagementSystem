package com.ruoyi.taskmgt.mapper.po;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Table(name = "tm_template", uniqueConstraints = {
        @UniqueConstraint(name = "template_name_index", columnNames = "name")
})
@Entity
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class TemplatePo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    private String name;

    private Long appId;

    /** 模板描述 */
    private String description;

    /** 所有适用的机器人组ID,用逗号分隔*/
    @Column(columnDefinition = "varchar(255)")
    private String robotGroupId;

    /** 表单内容（JSON格式，定义任务参数） */
    @Column(columnDefinition = "text")
    private String formContent;

    /** 标准作业流程（JSON数组，定义步骤名称、顺序等） */
    @Column(columnDefinition = "text")
    private String workflow;

    /** 状态（0正常 1停用） */
    private Byte status;

    private String rule;
}
