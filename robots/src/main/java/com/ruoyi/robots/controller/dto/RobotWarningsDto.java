package com.ruoyi.robots.controller.dto;

import lombok.Data;

@Data
public class RobotWarningsDto {
    /** 预警ID */
    private Long id;

    private Long[] ids;

    /** 处理人 */
    private String resolveUser;

    /** 处理备注 */
    private String resolveNote;
}
