package com.ruoyi.mode.invoker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 模式切换请求 - 用于构建下发给机器人的 WebSocket 指令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeSwitchRequest {

    /** 操作类型 */
    private String action = "mode_switch";

    /** 目标模式ID */
    private Long modeId;

    /** 目标模式名称 */
    private String modeName;

    /** 模式参数（从 sys_mode_param 表读取），如 {"charge_strategy": "after_task", "charge_threshold": 20} */
    private Map<String, Object> params;

    // 工厂方法
    public static ModeSwitchRequest of(Long modeId, String modeName, Map<String, Object> params) {
        ModeSwitchRequest req = new ModeSwitchRequest();
        req.setModeId(modeId);
        req.setModeName(modeName);
        req.setParams(params);
        return req;
    }
}
