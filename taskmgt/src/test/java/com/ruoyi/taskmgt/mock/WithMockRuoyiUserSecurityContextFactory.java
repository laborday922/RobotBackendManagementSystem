package com.ruoyi.taskmgt.mock;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WithMockRuoyiUserSecurityContextFactory implements WithSecurityContextFactory<WithMockRuoyiUser> {

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    @Override
    public SecurityContext createSecurityContext(WithMockRuoyiUser annotation) {
        // 构造权限列表
        List<GrantedAuthority> authorities = Arrays.stream(annotation.authorities())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(Long.valueOf(annotation.userId()));
        SysUser user = new SysUser();
        user.setUserId(Long.valueOf(annotation.userId()));
        user.setUserName(annotation.username());
        loginUser.setUser(user);
        loginUser.setDeptId(Long.valueOf(annotation.deptId()));
        // 可根据需要设置更多字段，如 roles、permissions 等

        // 创建 Authentication 对象
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
        authentication.setDetails(loginUser);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}