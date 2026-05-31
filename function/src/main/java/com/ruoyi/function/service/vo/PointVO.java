package com.ruoyi.function.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 点位视图对象（前端展示用）
 */
@ApiModel(description = "点位视图对象")
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
    private String coordinateStr;

    @ApiModelProperty("X坐标")
    private BigDecimal coordinateX;

    @ApiModelProperty("Y坐标")
    private BigDecimal coordinateY;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    public Long getSysPointId() {
        return sysPointId;
    }

    public void setSysPointId(Long sysPointId) {
        this.sysPointId = sysPointId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointTypeLabel() {
        return pointTypeLabel;
    }

    public void setPointTypeLabel(String pointTypeLabel) {
        this.pointTypeLabel = pointTypeLabel;
    }

    public String getCoordinateStr() {
        if (coordinateX != null && coordinateY != null) {
            return coordinateX + ", " + coordinateY;
        }
        return coordinateStr;
    }

    public void setCoordinateStr(String coordinateStr) {
        this.coordinateStr = coordinateStr;
    }

    public BigDecimal getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(BigDecimal coordinateX) {
        this.coordinateX = coordinateX;
    }

    public BigDecimal getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(BigDecimal coordinateY) {
        this.coordinateY = coordinateY;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
