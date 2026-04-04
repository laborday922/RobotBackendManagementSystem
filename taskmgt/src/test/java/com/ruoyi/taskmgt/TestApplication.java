package com.ruoyi.taskmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 测试环境专用启动类
 * - 排除定时任务配置（避免干扰）
 * - 排除 RocketMQ 等外部依赖（可选）
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.ruoyi.taskmgt", "com.ruoyi.common", "com.ruoyi.robots", "com.ruoyi.app"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.ruoyi.quartz.*"), @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.ruoyi.mq.*")
})
@MapperScan(basePackages = {"com.ruoyi.taskmgt.mapper", "com.ruoyi.app.mapper", "com.ruoyi.robots.mapper"})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}