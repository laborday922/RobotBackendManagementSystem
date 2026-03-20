package com.ruoyi.taskmgt.service.vo;

import com.ruoyi.common.clonefactory.CopyFrom;
import com.ruoyi.taskmgt.domain.bo.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@CopyFrom({Task.class})
public class TaskAbnormalVo extends TaskVo {
    private String robotStatusSummary;          // 机器人状态摘要/异常原因
    private List<RobotStatus> robotStatuses;    // 具体机器人状态列表
}