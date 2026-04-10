package com.ruoyi.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${ruoyi.map.upload-path:./uploads/map/}")
    private String mapUploadPath;

    @Value("${ruoyi.profile:./uploads/}")
    private String profile;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置地图图片访问路径
        registry.addResourceHandler("/uploads/map/**")
                .addResourceLocations("file:" + mapUploadPath)
                .setCachePeriod(0);

        // 配置若依框架的上传文件访问路径
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + profile);

        System.out.println("========== 静态资源映射配置 ==========");
        System.out.println("/uploads/map/** -> file:" + mapUploadPath);
        System.out.println("/profile/** -> file:" + profile);
        System.out.println("====================================");
    }
}