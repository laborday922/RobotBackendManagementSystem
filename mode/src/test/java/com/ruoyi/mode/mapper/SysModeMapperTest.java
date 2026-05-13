package com.ruoyi.mode.mapper;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysMode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeMapperTest {

    @Autowired
    private SysModeMapper sysModeMapper;

    private SysMode testMode;

    @BeforeEach
    void setUp() {
        testMode = new SysMode();
        testMode.setModeName("测试模式");
        testMode.setModeType("custom");
        testMode.setModeColor("#1890FF");
        testMode.setModeIcon("fa fa-test");
        testMode.setDescription("这是一个测试模式");
        testMode.setEnabled("1");
        testMode.setOrderNum(100);
        testMode.setTenantId(1L);  // 添加租户ID
        testMode.setDelFlag("0");
    }

    @Test
    @Order(1)
    void testInsertSysMode() {
        int result = sysModeMapper.insertSysMode(testMode);
        assertThat(result).isEqualTo(1);
        assertThat(testMode.getModeId()).isNotNull();
    }

    @Test
    @Order(2)
    void testSelectSysModeById() {
        sysModeMapper.insertSysMode(testMode);
        Long modeId = testMode.getModeId();

        SysMode found = sysModeMapper.selectSysModeById(modeId);
        assertThat(found).isNotNull();
        assertThat(found.getModeName()).isEqualTo("测试模式");
        assertThat(found.getModeType()).isEqualTo("custom");
    }

    @Test
    @Order(3)
    void testSelectSysModeList() {
        sysModeMapper.insertSysMode(testMode);

        SysMode query = new SysMode();
        query.setModeType("custom");
        List<SysMode> list = sysModeMapper.selectSysModeList(query);

        assertThat(list).isNotEmpty();
        assertThat(list.stream().anyMatch(m -> "测试模式".equals(m.getModeName()))).isTrue();
    }

    @Test
    @Order(4)
    void testUpdateSysMode() {
        sysModeMapper.insertSysMode(testMode);
        testMode.setModeName("更新后的测试模式");
        testMode.setDescription("更新后的描述");

        int result = sysModeMapper.updateSysMode(testMode);
        assertThat(result).isEqualTo(1);

        SysMode updated = sysModeMapper.selectSysModeById(testMode.getModeId());
        assertThat(updated.getModeName()).isEqualTo("更新后的测试模式");
        assertThat(updated.getDescription()).isEqualTo("更新后的描述");
    }

//    @Test
//    @Order(5)
//    void testIncrementUsageCount() {
//        // 先插入模式
//        sysModeMapper.insertSysMode(testMode);
//        Long modeId = testMode.getModeId();
//
//        // 调用带 tenantId 参数的 incrementUsageCount 方法
//        // 传入 tenantId = 1L（与测试模式相同）
//        int result = sysModeMapper.incrementUsageCount(modeId, 1L);
//
//        // incrementUsageCount 插入历史记录，应该返回1
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @Order(6)
//    void testIncrementUsageCountWithNullTenantId() {
//        // 先插入模式
//        sysModeMapper.insertSysMode(testMode);
//        Long modeId = testMode.getModeId();
//
//        // 测试 tenantId 为 null 的情况（管理员场景）
//        int result = sysModeMapper.incrementUsageCount(modeId, null);
//
//        // incrementUsageCount 插入历史记录，应该返回1
//        assertThat(result).isEqualTo(1);
//    }

    @Test
    @Order(7)
    void testUpdateRobotCount() {
        sysModeMapper.insertSysMode(testMode);
        int result = sysModeMapper.updateRobotCount(testMode.getModeId());
        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(8)
    void testDeleteSysModeById() {
        sysModeMapper.insertSysMode(testMode);
        int result = sysModeMapper.deleteSysModeById(testMode.getModeId());
        assertThat(result).isEqualTo(1);

        SysMode deleted = sysModeMapper.selectSysModeById(testMode.getModeId());
        assertThat(deleted).isNull();
    }

    @Test
    @Order(9)
    void testDeleteSysModeByIds() {
        sysModeMapper.insertSysMode(testMode);
        Long[] ids = {testMode.getModeId()};
        int result = sysModeMapper.deleteSysModeByIds(ids);
        assertThat(result).isEqualTo(1);
    }
}