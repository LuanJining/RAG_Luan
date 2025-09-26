package com.luanjining.rag.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus配置类 - 简化版本
 */
@Configuration
@MapperScan("com.luanjining.rag.mapper")
public class MybatisPlusConfig {

    // 暂时移除分页插件配置，使用基础功能
    // 如果需要分页功能，可以在Service层使用Page对象

}