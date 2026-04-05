package com.ruoyi.data.dashboard.mapper.po;

import lombok.Data;

@Data
public class RobotPositionLatestPo {

    private Long robotId;

    private String locationArea;

    private String specificLocation;

    private Double coordinateX;

    private Double coordinateY;

    private Double moveSpeed;

    private String statusDesc;

}