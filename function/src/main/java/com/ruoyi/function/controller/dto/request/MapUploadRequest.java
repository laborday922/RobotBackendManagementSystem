package com.ruoyi.function.controller.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * 地图上传请求DTO
 */
@ApiModel(description = "地图上传请求")
public class MapUploadRequest {

    @ApiModelProperty(value = "地图文件", required = true)
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    @ApiModelProperty(value = "地图ID（更新时传入）")
    private Long mapId;

    @ApiModelProperty(value = "地图名称（可选，不传则自动生成）")
    private String mapName;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Long getMapId() {
        return mapId;
    }

    public void setMapId(Long mapId) {
        this.mapId = mapId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
}