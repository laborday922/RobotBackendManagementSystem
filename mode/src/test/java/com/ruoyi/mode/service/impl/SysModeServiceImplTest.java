package com.ruoyi.mode.service.impl;

import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.domain.SysModeParam;
import com.ruoyi.mode.mapper.SysModeMapper;
import com.ruoyi.mode.mapper.SysModeParamMapper;
import com.ruoyi.mode.service.ISysModeService;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("模式Service测试")
class SysModeServiceImplTest {

    @Mock
    private SysModeMapper sysModeMapper;

    @Mock
    private SysModeParamMapper sysModeParamMapper;

    @InjectMocks
    private SysModeServiceImpl sysModeService;

    private SysMode testMode;
    private List<SysModeParam> testParams;

    @BeforeEach
    void setUp() {
        testMode = TestDataBuilder.buildTestMode(1L);
        testParams = new ArrayList<>();

        SysModeParam param = new SysModeParam();
        param.setParamId(1L);
        param.setModeId(1L);
        param.setParamName("测试参数");
        param.setParamType("string");
        param.setParamValue("test");
        testParams.add(param);
    }

    @Test
    @DisplayName("测试根据ID查询模式 - 成功")
    void testSelectSysModeById_Success() {
        when(sysModeMapper.selectSysModeById(1L)).thenReturn(testMode);
        when(sysModeParamMapper.selectSysModeParamByModeId(1L)).thenReturn(testParams);

        SysMode result = sysModeService.selectSysModeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getModeId()).isEqualTo(1L);
        assertThat(result.getModeParams()).isNotEmpty();
        verify(sysModeMapper).selectSysModeById(1L);
        verify(sysModeParamMapper).selectSysModeParamByModeId(1L);
    }

    @Test
    @DisplayName("测试根据ID查询模式 - 不存在")
    void testSelectSysModeById_NotFound() {
        when(sysModeMapper.selectSysModeById(999L)).thenReturn(null);

        SysMode result = sysModeService.selectSysModeById(999L);

        assertThat(result).isNull();
        verify(sysModeMapper).selectSysModeById(999L);
        verify(sysModeParamMapper, never()).selectSysModeParamByModeId(anyLong());
    }

    @Test
    @DisplayName("测试查询模式列表")
    void testSelectSysModeList() {
        List<SysMode> mockList = TestDataBuilder.buildTestModeList(5);
        when(sysModeMapper.selectSysModeList(any(SysMode.class))).thenReturn(mockList);
        when(sysModeParamMapper.selectSysModeParamByModeId(anyLong())).thenReturn(new ArrayList<>());

        SysMode query = new SysMode();
        query.setEnabled("1");

        List<SysMode> result = sysModeService.selectSysModeList(query);

        assertThat(result).hasSize(5);
        verify(sysModeMapper).selectSysModeList(query);
    }

    @Test
    @DisplayName("测试新增模式 - 带参数")
    void testInsertSysMode_WithParams() {
        testMode.setModeParams(testParams);

        when(sysModeMapper.insertSysMode(any(SysMode.class))).thenReturn(1);
        when(sysModeParamMapper.insertSysModeParam(any(SysModeParam.class))).thenReturn(1);

        int result = sysModeService.insertSysMode(testMode);

        assertThat(result).isEqualTo(1);
        verify(sysModeMapper).insertSysMode(testMode);
        verify(sysModeParamMapper, times(testParams.size())).insertSysModeParam(any(SysModeParam.class));
    }

    @Test
    @DisplayName("测试新增模式 - 不带参数")
    void testInsertSysMode_WithoutParams() {
        testMode.setModeParams(null);

        when(sysModeMapper.insertSysMode(any(SysMode.class))).thenReturn(1);

        int result = sysModeService.insertSysMode(testMode);

        assertThat(result).isEqualTo(1);
        verify(sysModeMapper).insertSysMode(testMode);
        verify(sysModeParamMapper, never()).insertSysModeParam(any());
    }

    @Test
    @DisplayName("测试更新模式")
    void testUpdateSysMode() {
        testMode.setModeParams(testParams);

        when(sysModeMapper.updateSysMode(any(SysMode.class))).thenReturn(1);
        // 注意：deleteSysModeParamByModeId 返回 int，不是 void
        when(sysModeParamMapper.deleteSysModeParamByModeId(anyLong())).thenReturn(1);
        when(sysModeParamMapper.insertSysModeParam(any(SysModeParam.class))).thenReturn(1);

        int result = sysModeService.updateSysMode(testMode);

        assertThat(result).isEqualTo(1);
        verify(sysModeMapper).updateSysMode(testMode);
        verify(sysModeParamMapper).deleteSysModeParamByModeId(testMode.getModeId());
        verify(sysModeParamMapper, times(testParams.size())).insertSysModeParam(any(SysModeParam.class));
    }

    @Test
    @DisplayName("测试批量删除模式")
    void testDeleteSysModeByIds() {
        Long[] ids = {1L, 2L, 3L};

        when(sysModeMapper.deleteSysModeByIds(ids)).thenReturn(3);
        // 注意：deleteSysModeParamByModeId 返回 int，不是 void
        when(sysModeParamMapper.deleteSysModeParamByModeId(anyLong())).thenReturn(1);

        int result = sysModeService.deleteSysModeByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(sysModeParamMapper, times(ids.length)).deleteSysModeParamByModeId(anyLong());
        verify(sysModeMapper).deleteSysModeByIds(ids);
    }

    @Test
    @DisplayName("测试启用/禁用模式")
    void testChangeModeStatus() {
        when(sysModeMapper.updateSysMode(any(SysMode.class))).thenReturn(1);

        int result = sysModeService.changeModeStatus(1L, "0");

        assertThat(result).isEqualTo(1);
        verify(sysModeMapper).updateSysMode(argThat(mode ->
                mode.getModeId().equals(1L) && "0".equals(mode.getEnabled())
        ));
    }
}