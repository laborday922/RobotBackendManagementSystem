package com.ruoyi.data.dashboard.controller.vo;

import lombok.Data;

@Data
public class RobotGeoInfo {

    /**
     * 机器人ID
     */
    private Long robotId;

    /**
     * 区域
     */
    private String locationArea;

    /**
     * 具体位置
     */
    private String specificLocation;

    /**
     * X坐标
     */
    private Double coordinateX;

    /**
     * Y坐标
     */
    private Double coordinateY;

    /**
     * 移动速度
     */
    private Double moveSpeed;

    /**
     * 状态描述
     */
    private String statusDesc;

}