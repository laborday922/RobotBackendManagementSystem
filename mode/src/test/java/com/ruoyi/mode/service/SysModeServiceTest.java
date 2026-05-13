package com.ruoyi.mode.service;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.domain.SysModeParam;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeServiceTest {

    @Autowired
    private ISysModeService sysModeService;

    private SysMode testMode;

    @BeforeEach
    void setUp() {
        testMode = new SysMode();
        testMode.setModeName("服务测试模式");
        testMode.setModeType("custom");
        testMode.setModeColor("#FF5733");
        testMode.setModeIcon("fa fa-service-test");
        testMode.setDescription("服务层测试模式");
        testMode.setEnabled("1");
        testMode.setOrderNum(200);

        List<SysModeParam> params = new ArrayList<>();
        SysModeParam param = new SysModeParam();
        param.setParamName("测试参数");
        param.setParamType("string");
        param.setParamValue("默认值");
        params.add(param);
        testMode.setModeParams(params);
    }

    @Test
    @Order(1)
    void testInsertSysMode() {
        int result = sysModeService.insertSysMode(testMode);
        assertThat(result).isEqualTo(1);
        assertThat(testMode.getModeId()).isNotNull();
    }

    @Test
    @Order(2)
    void testSelectSysModeById() {
        sysModeService.insertSysMode(testMode);
        Long modeId = testMode.getModeId();

        SysMode found = sysModeService.selectSysModeById(modeId);
        assertThat(found).isNotNull();
        assertThat(found.getModeName()).isEqualTo("服务测试模式");
        assertThat(found.getModeParams()).isNotEmpty();
    }

    @Test
    @Order(3)
    void testSelectSysModeList() {
        sysModeService.insertSysMode(testMode);

        SysMode query = new SysMode();
        query.setModeType("custom");
        List<SysMode> list = sysModeService.selectSysModeList(query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(4)
    void testUpdateSysMode() {
        sysModeService.insertSysMode(testMode);
        testMode.setModeName("更新后的服务测试模式");
        testMode.setDescription("更新后的描述");

        int result = sysModeService.updateSysMode(testMode);
        assertThat(result).isEqualTo(1);

        SysMode updated = sysModeService.selectSysModeById(testMode.getModeId());
        assertThat(updated.getModeName()).isEqualTo("更新后的服务测试模式");
    }

    @Test
    @Order(5)
    void testChangeModeStatus() {
        sysModeService.insertSysMode(testMode);
        int result = sysModeService.changeModeStatus(testMode.getModeId(), "0");
        assertThat(result).isEqualTo(1);

        SysMode updated = sysModeService.selectSysModeById(testMode.getModeId());
        assertThat(updated.getEnabled()).isEqualTo("0");
    }

    @Test
    @Order(6)
    void testIncrementUsageCount() {
        sysModeService.insertSysMode(testMode);
        int result = sysModeService.incrementUsageCount(testMode.getModeId());
        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(7)
    void testUpdateRobotCountByModeId() {
        sysModeService.insertSysMode(testMode);
        // 不会抛出异常即可
        sysModeService.updateRobotCountByModeId(testMode.getModeId());
    }

    @Test
    @Order(8)
    void testDeleteSysModeById() {
        sysModeService.insertSysMode(testMode);
        int result = sysModeService.deleteSysModeById(testMode.getModeId());
        assertThat(result).isEqualTo(1);
    }
}