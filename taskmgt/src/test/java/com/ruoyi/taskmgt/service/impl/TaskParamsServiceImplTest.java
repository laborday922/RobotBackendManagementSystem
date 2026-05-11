package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.app.domain.TAppApi;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.domain.bo.ApiParamDef;
import com.ruoyi.taskmgt.invoker.dto.OptionDef;
import com.ruoyi.taskmgt.invoker.dto.RobotParamDef;
import com.ruoyi.taskmgt.invoker.dto.RobotParamsResponse;
import com.ruoyi.taskmgt.service.vo.DynamicParamVo;
import com.ruoyi.taskmgt.service.vo.ParamOption;
import com.ruoyi.taskmgt.websocket.TaskRobotWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskParamsServiceImplTest {

    @Mock
    private TaskRobotWebSocketHandler webSocketHandler;
    @Mock
    private ITAppApiService appApiService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TaskParamsServiceImpl taskParamsService;

    private static final Long API_ID = 1L;
    private static final Long ROBOT_ID = 100L;

    @Test
    void testGetApiParamDefs_Success() throws Exception {
        String paramsSchema = "{\"key1\":{\"type\":\"string\",\"valueSource\":\"DYNAMIC\",\"description\":\"desc\"}}";
        TAppApi api = new TAppApi();
        api.setParamsSchema(paramsSchema);

        when(appApiService.selectTAppApiById(API_ID)).thenReturn(api);
        Map<String, ApiParamDef> expected = new HashMap<>();
        ApiParamDef def = new ApiParamDef();
        def.setType("string");
        def.setValueSource("DYNAMIC");
        def.setDescription("desc");
        expected.put("key1", def);
        when(objectMapper.readValue(eq(paramsSchema), any(TypeReference.class))).thenReturn(expected);

        Map<String, ApiParamDef> result = taskParamsService.getApiParamDefs(API_ID);
        assertThat(result).containsKey("key1");
        assertThat(result.get("key1").getValueSource()).isEqualTo("DYNAMIC");
    }

    @Test
    void testGetApiParamDefs_ApiNull() {
        when(appApiService.selectTAppApiById(API_ID)).thenReturn(null);
        Map<String, ApiParamDef> result = taskParamsService.getApiParamDefs(API_ID);
        assertThat(result).isEmpty();
    }

    @Test
    void testGetApiParamDefs_JsonParseError() throws Exception {
        TAppApi api = new TAppApi();
        api.setParamsSchema("invalid json");
        when(appApiService.selectTAppApiById(API_ID)).thenReturn(api);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenThrow(JsonProcessingException.class);

        Map<String, ApiParamDef> result = taskParamsService.getApiParamDefs(API_ID);
        assertThat(result).isEmpty();
    }

    @Test
    void testGetDynamicParams_NoDynamicParams() throws Exception {
        String paramsSchema = "{\"key1\":{\"type\":\"string\",\"valueSource\":\"FIXED\"}}";
        TAppApi api = new TAppApi();
        api.setParamsSchema(paramsSchema);
        when(appApiService.selectTAppApiById(API_ID)).thenReturn(api);
        Map<String, ApiParamDef> paramDefs = new HashMap<>();
        ApiParamDef def = new ApiParamDef();
        def.setValueSource("FIXED");
        paramDefs.put("key1", def);
        when(objectMapper.readValue(eq(paramsSchema), any(TypeReference.class))).thenReturn(paramDefs);

        List<DynamicParamVo> result = taskParamsService.getDynamicParams(API_ID, ROBOT_ID);
        assertThat(result).isEmpty();
    }

    @Test
    void testGetDynamicParams_WithRobotOnline() throws Exception {
        // 准备参数定义
        String paramsSchema = "{\"dynamicKey\":{\"type\":\"select\",\"valueSource\":\"DYNAMIC\",\"description\":\"动态参数\",\"required\":true}}";
        TAppApi api = new TAppApi();
        api.setId(API_ID);
        api.setParamsSchema(paramsSchema);
        when(appApiService.selectTAppApiById(API_ID)).thenReturn(api);

        Map<String, ApiParamDef> paramDefs = new HashMap<>();
        ApiParamDef def = new ApiParamDef();
        def.setValueSource("DYNAMIC");
        def.setType("select");
        def.setDescription("动态参数");
        def.setRequired(true);
        def.setDynamicConfig(new HashMap<>());
        paramDefs.put("dynamicKey", def);
        when(objectMapper.readValue(eq(paramsSchema), any(TypeReference.class))).thenReturn(paramDefs);

        // Mock WebSocket 在线
        when(webSocketHandler.isOnline(ROBOT_ID)).thenReturn(true);

        // Mock 机器人返回选项
        RobotParamsResponse resp = new RobotParamsResponse();
        RobotParamDef paramDef = RobotParamDef.builder()
                .name("dynamicKey")
                .type("select")
                .options(Arrays.asList(
                        new OptionDef() {{ setId("1"); setName("选项1"); }},
                        new OptionDef() {{ setId("2"); setName("选项2"); }}
                ))
                .build();
        resp.setParams(Collections.singletonList(paramDef));

        RobotWebSocketMessage wsMessage = new RobotWebSocketMessage();
        wsMessage.setData(resp);
        CompletableFuture<RobotWebSocketMessage> future = CompletableFuture.completedFuture(wsMessage);

        when(webSocketHandler.sendAndWaitRaw(anyLong(), anyMap(), anyString(), anyLong()))
                .thenReturn(future);

        doReturn(resp).when(objectMapper).convertValue(any(), any(TypeReference.class));

        List<DynamicParamVo> result = taskParamsService.getDynamicParams(API_ID, ROBOT_ID);

        assertThat(result).hasSize(1);
        DynamicParamVo vo = result.get(0);
        assertThat(vo.getParamKey()).isEqualTo("dynamicKey");
        assertThat(vo.getOptions()).hasSize(2);
        ParamOption option = vo.getOptions().get(0);
        assertThat(option.getValue()).isEqualTo("1");
        assertThat(option.getLabel()).isEqualTo("选项1");
    }
    @Test
    void testGetDynamicParams_RobotOffline() throws Exception {
        String paramsSchema = "{\"dynamicKey\":{\"valueSource\":\"DYNAMIC\"}}";
        TAppApi api = new TAppApi();
        api.setParamsSchema(paramsSchema);
        when(appApiService.selectTAppApiById(API_ID)).thenReturn(api);

        Map<String, ApiParamDef> paramDefs = new HashMap<>();
        ApiParamDef def = new ApiParamDef();
        def.setValueSource("DYNAMIC");
        paramDefs.put("dynamicKey", def);
        when(objectMapper.readValue(eq(paramsSchema), any(TypeReference.class))).thenReturn(paramDefs);

        when(webSocketHandler.isOnline(ROBOT_ID)).thenReturn(false);

        List<DynamicParamVo> result = taskParamsService.getDynamicParams(API_ID, ROBOT_ID);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOptions()).isEmpty();
    }
}