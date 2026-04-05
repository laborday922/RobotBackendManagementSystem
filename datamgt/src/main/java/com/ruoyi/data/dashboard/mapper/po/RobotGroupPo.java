package com.ruoyi.data.dashboard.mapper.po;

import lombok.Data;

@Data
public class RobotGroupPo {

    private Integer id;

    private String name;

    private String description;

    private Integer onlineCount;   // 在线机器人数量

    private Integer totalCount;    // 总机器人数量
}