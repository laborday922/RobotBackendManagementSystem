package com.ruoyi.function;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class TestMyBatisConfig {

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 设置类型别名包
        sessionFactory.setTypeAliasesPackage("com.ruoyi.function.domain");

        // 手动注册所有需要的类型别名（解决找不到类的问题）
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);

        // 注册所有 domain 类的别名
        configuration.getTypeAliasRegistry().registerAlias("SysMap", com.ruoyi.function.domain.SysMap.class);
        configuration.getTypeAliasRegistry().registerAlias("SysPoint", com.ruoyi.function.domain.SysPoint.class);
        configuration.getTypeAliasRegistry().registerAlias("SysNavConfig", com.ruoyi.function.domain.SysNavConfig.class);
        configuration.getTypeAliasRegistry().registerAlias("SysReceptionConfig", com.ruoyi.function.domain.SysReceptionConfig.class);
        configuration.getTypeAliasRegistry().registerAlias("SysTourContent", com.ruoyi.function.domain.SysTourContent.class);
        configuration.getTypeAliasRegistry().registerAlias("SysTourGeneral", com.ruoyi.function.domain.SysTourGeneral.class);
        configuration.getTypeAliasRegistry().registerAlias("SysTourRoute", com.ruoyi.function.domain.SysTourRoute.class);
        configuration.getTypeAliasRegistry().registerAlias("SysRoutePoint", com.ruoyi.function.domain.SysRoutePoint.class);

        sessionFactory.setConfiguration(configuration);

        // 只扫描 function 模块的 mapper XML，避免扫描 system 模块
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // 合并所有 mapper 位置到一个数组
        List<Resource> resources = new ArrayList<>();

        // 添加所有需要的 mapper XML
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysMapMapper.xml");
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysPointMapper.xml");
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysNavConfigMapper.xml");
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysReceptionConfigMapper.xml");
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysTourContentMapper.xml");
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysTourGeneralMapper.xml");
        addMapperResources(resolver, resources, "classpath*:mapper/**/SysTourRouteMapper.xml");

        sessionFactory.setMapperLocations(resources.toArray(new Resource[0]));

        return sessionFactory.getObject();
    }

    private void addMapperResources(PathMatchingResourcePatternResolver resolver, List<Resource> resources, String pattern) {
        try {
            Resource[] mappers = resolver.getResources(pattern);
            for (Resource mapper : mappers) {
                resources.add(mapper);
            }
        } catch (Exception e) {
            // 忽略找不到的资源
            System.out.println("Warning: Could not load mapper pattern: " + pattern);
        }
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer scanner = new MapperScannerConfigurer();
        scanner.setBasePackage("com.ruoyi.function.mapper");
        scanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return scanner;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}