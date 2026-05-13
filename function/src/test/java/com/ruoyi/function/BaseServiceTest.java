package com.ruoyi.function;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
public abstract class BaseServiceTest {

    @BeforeEach
    void setUpSecurityContext() {
        // 1. 创建 SysUser（若依用户实体）
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName("admin");
        sysUser.setNickName("管理员");

        // 2. 设置权限
        Set<String> permissions = new HashSet<>();
        permissions.add("*:*:*");

        // 3. 创建 LoginUser（若依自定义用户对象，这是 SecurityUtils 期望的类型）
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setDeptId(1L);
        loginUser.setUser(sysUser);
        loginUser.setPermissions(permissions);

        // 4. 创建 Spring Security 权限
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // 5. 创建 Authentication，使用 LoginUser 作为 principal
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);

        // 6. 设置到 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 调试输出
        System.out.println("=== BaseServiceTest SecurityContext 设置完成 ===");
        System.out.println("用户类型: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName());
        System.out.println("用户名: " + loginUser.getUsername());
    }
}