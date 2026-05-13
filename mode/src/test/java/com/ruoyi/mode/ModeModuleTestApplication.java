package com.ruoyi.mode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class
})
@ComponentScan(
        basePackages = {
                "com.ruoyi.mode",
                "com.ruoyi.common.core",
                "com.ruoyi.common.utils",
                "com.ruoyi.common.datasource",
                "com.ruoyi.common.annotation",
                "com.ruoyi.common.enums",
                "com.ruoyi.common.exception",
                "com.ruoyi.common.threadlocal"
        },
        excludeFilters = {
                // 排除 framework 包
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.ruoyi\\.framework\\..*"
                ),
                // 排除 app 模块
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.ruoyi\\.app\\..*"
                ),
                // 排除 taskmgt 模块（包括 WebSocket）
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.ruoyi\\.taskmgt\\..*"
                ),
                // 排除 robots 模块
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.ruoyi\\.robots\\..*"
                )
        }
)
// 关键：只扫描 mode 模块的 Mapper
@MapperScan(basePackages = {
        "com.ruoyi.mode.mapper"
})
@EnableAsync
@EnableScheduling
public class ModeModuleTestApplication {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");
    }

    public static void main(String[] args) {
        SpringApplication.run(ModeModuleTestApplication.class, args);
    }
}