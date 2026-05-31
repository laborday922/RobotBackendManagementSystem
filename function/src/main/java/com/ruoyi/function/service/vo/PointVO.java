package com.ruoyi.function.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 点位视图对象（前端展示用）
 */
@ApiModel(description = "点位视图对象")
@Getter
@Setter
public class PointVO {

    @ApiModelProperty("系统点位ID")
    private Long sysPointId;

    @ApiModelProperty("点位名称")
    private String pointName;

    @ApiModelProperty("点位编码")
    private String pointCode;

    @ApiModelProperty("点位类型标签")
    private String pointTypeLabel;

    @ApiModelProperty("坐标字符串（x, y）")
    @Getter(AccessLevel.NONE)
    private String coordinateStr;

    @ApiModelProperty("X坐标")
    private BigDecimal coordinateX;

    @ApiModelProperty("Y坐标")
    private BigDecimal coordinateY;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    public String getCoordinateStr() {
        if (coordinateX != null && coordinateY != null) {
            return coordinateX + ", " + coordinateY;
        }
        return coordinateStr;
    }
}
