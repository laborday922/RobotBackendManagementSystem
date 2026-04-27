package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.app.domain.TAppApi;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.domain.bo.ApiParamDef;
import com.ruoyi.taskmgt.service.vo.DynamicParamVo;
import com.ruoyi.taskmgt.service.vo.ParamOption;
import com.ruoyi.taskmgt.invoker.dto.RobotParamDef;
import com.ruoyi.taskmgt.invoker.dto.RobotParamsResponse;
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
    private final ITAppApiService appApiService;
    private final ObjectMapper objectMapper;
    @Override
    public Map<String, ApiParamDef> getApiParamDefs(Long apiId) {
        TAppApi api = appApiService.selectTAppApiById(apiId);
        if (api == null || StringUtils.isBlank(api.getParamsSchema())) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(api.getParamsSchema(),
                    new TypeReference<Map<String, ApiParamDef>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析 API 参数定义失败 apiId={}", apiId, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<DynamicParamVo> getDynamicParams(Long apiId, Long robotId) {
        Map<String, ApiParamDef> paramDefs = getApiParamDefs(apiId);
        List<DynamicParamVo> vos = new ArrayList<>();

        for (Map.Entry<String, ApiParamDef> entry : paramDefs.entrySet()) {
            ApiParamDef def = entry.getValue();
            if (!"DYNAMIC".equals(def.getValueSource())) {
                continue;
            }
            DynamicParamVo vo = new DynamicParamVo();
            vo.setParamKey(entry.getKey());
            vo.setParamName(def.getDescription());
            vo.setParamType(def.getType());
            vo.setRequired(def.getRequired());

            if (robotId != null && webSocketHandler.isOnline(robotId)) {
                List<ParamOption> options = fetchRobotOptions(robotId, apiId, entry.getKey(), def.getDynamicConfig());
                vo.setOptions(options);
            } else {
                vo.setOptions(Collections.emptyList());
            }
            vos.add(vo);
        }
        return vos;
    }

    private List<ParamOption> fetchRobotOptions(Long robotId, Long apiId, String paramKey, Map<String, Object> dynamicConfig) {
        if (robotId == null || !webSocketHandler.isOnline(robotId)) {
            return Collections.emptyList();
        }
        Map<String, List<ParamOption>> allOptions = fetchRobotDynamicOptions(apiId, robotId);
        return allOptions.getOrDefault(paramKey, Collections.emptyList());
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
