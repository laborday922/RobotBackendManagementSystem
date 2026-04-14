package com.ruoyi.mode.invoker.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 模式切换请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeSwitchRequest {

    /** 操作类型：mode_switch */
    private String action = "mode_switch";

    /** 目标模式ID */
    private Long modeId;

    /** 目标模式名称 */
    private String modeName;

    /** 是否立即执行 */
    private Boolean immediate = true;

    /** 延迟执行时间（毫秒） */
    private Long delayMs;

    /** 回调URL（用于异步模式） */
    private String callbackUrl;

    // 工厂方法
    public static ModeSwitchRequest of(Long modeId, String modeName) {
        ModeSwitchRequest req = new ModeSwitchRequest();
        req.setModeId(modeId);
        req.setModeName(modeName);
        return req;
    }
}