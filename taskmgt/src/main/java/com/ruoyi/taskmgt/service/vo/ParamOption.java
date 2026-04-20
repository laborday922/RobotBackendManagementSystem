package com.ruoyi.taskmgt.service.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParamOption {
    private String value;  // 提交值，如位置ID
    private String label;  // 显示文本
}
