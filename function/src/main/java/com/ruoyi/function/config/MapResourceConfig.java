package com.ruoyi.function.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MapResourceConfig implements WebMvcConfigurer {

    @Value("${ruoyi.map.upload-path:./uploads/map/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置地图图片访问路径
        registry.addResourceHandler("/uploads/map/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(0); // 开发环境设置0，避免缓存

        System.out.println("========== MapResourceConfig 已加载 ==========");
        System.out.println("映射: /uploads/map/** -> file:" + uploadPath);
        System.out.println("=============================================");
    }
}