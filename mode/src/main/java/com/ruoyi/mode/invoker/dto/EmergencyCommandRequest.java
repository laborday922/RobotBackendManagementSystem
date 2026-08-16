package com.ruoyi.mode.invoker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 紧急操作指令 - 用于构建下发给机器人的 WebSocket 指令（action=emergency）
 *
 * <p>与模式切换（{@link ModeSwitchRequest}）类似，机器人通过 {@code data.action} 识别指令类型，
 * 通过 {@code data.command} 区分具体紧急操作：</p>
 * <ul>
 *   <li>emergency_stop：紧急停止</li>
 *   <li>emergency_evacuation：紧急撤离</li>
 *   <li>restart：重启</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class EmergencyCommandRequest {

    /** 操作类型，统一为 emergency */
    private String action = "emergency";

    /** 紧急操作子类型：emergency_stop / emergency_evacuation / restart */
    private String command;

    public static EmergencyCommandRequest of(String command) {
        EmergencyCommandRequest req = new EmergencyCommandRequest();
        req.setCommand(command);
        return req;
    }
}
