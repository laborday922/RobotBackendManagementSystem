package com.ruoyi.mode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {ModeModuleTestApplication.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class ModeModuleApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文加载
    }
}