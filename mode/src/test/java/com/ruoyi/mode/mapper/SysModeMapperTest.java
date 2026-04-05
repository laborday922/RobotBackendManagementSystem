package com.ruoyi.mode.mapper;

import com.ruoyi.mode.TestApplication;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional  // 每个测试方法执行后自动回滚，不影响数据库
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysModeMapperTest {

    @Autowired
    private SysModeMapper sysModeMapper;

    private SysMode testMode;

    @BeforeEach
    void setUp() {
        testMode = TestDataBuilder.buildTestMode(null);
        testMode.setModeName("测试模式_JUnit_" + System.currentTimeMillis());  // 使用时间戳避免重复
    }

    @Test
    @DisplayName("测试插入模式")
    void testInsertSysMode() {
        int result = sysModeMapper.insertSysMode(testMode);
        assertThat(result).isEqualTo(1);
        assertThat(testMode.getModeId()).isNotNull();

        SysMode saved = sysModeMapper.selectSysModeById(testMode.getModeId());
        assertThat(saved).isNotNull();
        assertThat(saved.getModeName()).isEqualTo(testMode.getModeName());
    }

    @Test
    @DisplayName("测试查询模式列表")
    void testSelectSysModeList() {
        sysModeMapper.insertSysMode(testMode);

        SysMode query = new SysMode();
        query.setModeType("custom");

        List<SysMode> modes = sysModeMapper.selectSysModeList(query);
        assertThat(modes).isNotEmpty();
    }

    @Test
    @DisplayName("测试根据ID查询模式")
    void testSelectSysModeById() {
        sysModeMapper.insertSysMode(testMode);

        SysMode found = sysModeMapper.selectSysModeById(testMode.getModeId());
        assertThat(found).isNotNull();
        assertThat(found.getModeId()).isEqualTo(testMode.getModeId());
    }

    @Test
    @DisplayName("测试更新模式")
    void testUpdateSysMode() {
        sysModeMapper.insertSysMode(testMode);

        String newName = "更新后的模式名称_" + System.currentTimeMillis();
        testMode.setModeName(newName);
        testMode.setEnabled("0");
        int result = sysModeMapper.updateSysMode(testMode);
        assertThat(result).isEqualTo(1);

        SysMode updated = sysModeMapper.selectSysModeById(testMode.getModeId());
        assertThat(updated.getModeName()).isEqualTo(newName);
        assertThat(updated.getEnabled()).isEqualTo("0");
    }

    @Test
    @DisplayName("测试删除模式")
    void testDeleteSysModeById() {
        sysModeMapper.insertSysMode(testMode);

        int result = sysModeMapper.deleteSysModeById(testMode.getModeId());
        assertThat(result).isEqualTo(1);

        SysMode deleted = sysModeMapper.selectSysModeById(testMode.getModeId());
        assertThat(deleted).isNull();
    }
}