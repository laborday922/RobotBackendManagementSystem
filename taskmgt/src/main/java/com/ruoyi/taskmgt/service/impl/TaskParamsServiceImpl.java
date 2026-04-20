package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.app.domain.TAppApi;
import com.ruoyi.app.domain.TAppParam;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.app.service.ITAppParamService;
import com.ruoyi.taskmgt.service.vo.DynamicParamVo;
import com.ruoyi.taskmgt.service.vo.ParamOption;
import com.ruoyi.taskmgt.service.vo.RobotParamDef;
import com.ruoyi.taskmgt.service.vo.RobotParamsResponse;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.taskmgt.service.ITaskParamsService;
import com.ruoyi.taskmgt.websocket.TaskRobotWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TaskParamsServiceImpl implements ITaskParamsService {
    private final TaskRobotWebSocketHandler webSocketHandler;
    private final ITAppParamService appParamService;
    private final ITAppApiService appApiService;
    private final ObjectMapper objectMapper;
    /**
     * 获取指定 API 的所有动态参数及实时选项
     *
     * @param apiId   API ID
     * @param robotId 机器人ID（用于实时查询）
     */
    @Override
    public List<DynamicParamVo> getDynamicParams(Long apiId, Long robotId) {
        // 查询该 API 下 value_source = 'DYNAMIC' 的参数
        TAppParam query = new TAppParam();
        query.setApiId(apiId);
        query.setValueSource("DYNAMIC");
        List<TAppParam> dynamicParams = appParamService.selectTAppParamList(query);
        if (dynamicParams.isEmpty()) {
            return Collections.emptyList();
        }
        // 从机器人获取实时选项（若 robotId 有效）
        Map<String, List<ParamOption>> robotOptionsMap = Collections.emptyMap();
        if (robotId != null && webSocketHandler.isOnline(robotId)) {
            robotOptionsMap = fetchRobotDynamicOptions(apiId, robotId);
        }
        // 组装返回 VO
        List<DynamicParamVo> vos = new ArrayList<>();
        for (TAppParam param : dynamicParams) {
            DynamicParamVo vo = new DynamicParamVo();
            vo.setParamId(param.getId());
            vo.setParamKey(param.getParamKey());
            vo.setParamName(param.getParamName());
            vo.setParamType(param.getParamType());
            List<ParamOption> options = robotOptionsMap.getOrDefault(param.getParamKey(), Collections.emptyList());
            vo.setOptions(options);
            vos.add(vo);
        }
        return vos;
    }
    private Map<String, List<ParamOption>> fetchRobotDynamicOptions(Long apiId, Long robotId) {
        TAppApi api = appApiService.selectTAppApiById(apiId);
        if (api == null) return Collections.emptyMap();
        // 构造 WebSocket 请求
        Map<String, Object> request = new HashMap<>();
        request.put("action", "getParams");
        request.put("operationId", api.getId());
        String correlationId = UUID.randomUUID().toString();
        try {
            CompletableFuture<RobotWebSocketMessage> future =
                    webSocketHandler.sendAndWaitRaw(robotId, request, correlationId, 10);
            RobotWebSocketMessage response = future.get(10, TimeUnit.SECONDS);
            if (response.getData() != null) {
                RobotParamsResponse paramsResp = objectMapper.convertValue(
                        response.getData(), new TypeReference<RobotParamsResponse>() {});
                return convertToOptionsMap(paramsResp);
            }
        } catch (Exception e) {
            log.error("获取机器人动态参数失败 apiId={}, robotId={}", apiId, robotId, e);
        }
        return Collections.emptyMap();
    }
    private Map<String, List<ParamOption>> convertToOptionsMap(RobotParamsResponse resp) {Map<String, List<ParamOption>> map = new HashMap<>();if (resp.getParams() != null) {
        for (RobotParamDef def : resp.getParams()) {
            if ("select".equals(def.getType()) && def.getOptions() != null) {
                List<ParamOption> options = def.getOptions().stream()
                        .map(opt -> new ParamOption(opt.getId(), opt.getName()))
                        .collect(Collectors.toList());
                map.put(def.getName(), options);
            }
        }
    }
        return map;
    }
}
