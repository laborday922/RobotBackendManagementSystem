package com.ruoyi.data.controller;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.mock.WithMockRuoyiUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class CleanRuleExecuteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisCache redisCache;

    @Test
    public void testExecute() throws Exception {
        mockMvc.perform(post("/clean/execute/clean/{id}", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").exists());
    }
}