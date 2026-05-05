package com.ruoyi.function.controller.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * 点位响应DTO
 */
@ApiModel(description = "点位响应")
public class PointResponse {

    @ApiModelProperty("点位ID")
    private Long pointId;

    @ApiModelProperty("所属地图ID")
    private Long mapId;

    @ApiModelProperty("点位名称")
    private String pointName;

    @ApiModelProperty("点位编码")
    private String pointCode;

    @ApiModelProperty("点位类型")
    private String pointType;

    @ApiModelProperty("点位类型文本")
    private String pointTypeText;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("状态文本")
    private String statusText;

    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("备注")
    private String remark;

    // Getters and Setters
    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }

    public Long getMapId() { return mapId; }
    public void setMapId(Long mapId) { this.mapId = mapId; }

    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }

    public String getPointCode() { return pointCode; }
    public void setPointCode(String pointCode) { this.pointCode = pointCode; }

    public String getPointType() { return pointType; }
    public void setPointType(String pointType) { this.pointType = pointType; }

    public String getPointTypeText() { return pointTypeText; }
    public void setPointTypeText(String pointTypeText) { this.pointTypeText = pointTypeText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}