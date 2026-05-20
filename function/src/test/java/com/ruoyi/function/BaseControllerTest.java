package com.ruoyi.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUpSecurityContext() {
        // 创建 SysUser 对象
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName("admin");
        sysUser.setNickName("管理员");

        // 创建 LoginUser 对象（SecurityUtils 期望的类型）
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setDeptId(1L);
        loginUser.setUser(sysUser);

        // 创建 Authentication 对象，使用 LoginUser 作为 principal
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        // 设置到 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 调试输出
        System.out.println("=== SecurityContext 设置完成 ===");
        System.out.println("LoginUser 用户名: " + loginUser.getUsername());
        System.out.println("Authentication principal: " +
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName());
    }
}