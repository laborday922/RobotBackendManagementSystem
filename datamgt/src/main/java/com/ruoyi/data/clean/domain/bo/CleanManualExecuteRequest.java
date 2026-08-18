package com.ruoyi.data.clean.domain.bo;

import lombok.Data;

/**
 * 手动立即执行请求参数
 */
@Data
public class CleanManualExecuteRequest {

    private String configJson;

    private String applyDataSource;
}
