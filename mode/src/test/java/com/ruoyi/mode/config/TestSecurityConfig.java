package com.ruoyi.mode.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class TestSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // 禁用 CSRF
                .authorizeRequests()
                .anyRequest().permitAll()  // 允许所有请求
                .and()
                .formLogin().disable()
                .httpBasic().disable();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        // 创建测试用户，拥有所有权限
        manager.createUser(User.withUsername("admin")
                .password("{noop}password")  // {noop} 表示明文密码
                .authorities(
                        "system:mode:list",
                        "system:mode:query",
                        "system:mode:add",
                        "system:mode:edit",
                        "system:mode:remove",
                        "system:robot:list",
                        "system:robot:query",
                        "system:robot:add",
                        "system:robot:edit",
                        "system:robot:remove"
                )
                .build());

        return manager;
    }
}