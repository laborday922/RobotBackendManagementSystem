package com.ruoyi.function;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@DisplayName("SecurityUtils 测试")
class SecurityUtilsTest {

    @BeforeEach
    void setUp() {
        // 创建 SysUser 对象（若依的用户实体）
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName("admin");
        sysUser.setNickName("管理员");

        // 设置权限
        Set<String> permissions = new HashSet<>();
        permissions.add("*:*:*");

        // 创建 LoginUser 对象（若依的自定义用户对象，SecurityUtils 期望的类型）
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setDeptId(1L);
        loginUser.setUser(sysUser);
        loginUser.setPermissions(permissions);

        // 创建权限列表用于 Spring Security
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // 创建 Authentication，使用 LoginUser 作为 principal
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);

        // 设置到 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("测试 SecurityUtils.getUsername() 是否能获取用户名")
    void testGetUsername() {
        String username = SecurityUtils.getUsername();
        System.out.println("获取到的用户名: " + username);
        assertThat(username).isEqualTo("admin");
    }

    @Test
    @DisplayName("测试 SecurityUtils.getUserId() 是否能获取用户ID")
    void testGetUserId() {
        Long userId = SecurityUtils.getUserId();
        System.out.println("获取到的用户ID: " + userId);
        assertThat(userId).isEqualTo(1L);
    }
}