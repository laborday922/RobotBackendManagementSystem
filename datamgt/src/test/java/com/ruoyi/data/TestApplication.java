package com.ruoyi.data;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.ruoyi.data.clean", "com.ruoyi.data.dashboard", "com.ruoyi.data.metric", "com.ruoyi.data.ai"})
@MapperScan("com.ruoyi.data.**.mapper")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}