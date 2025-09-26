package com.luanjining.rag.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = "com.luanjining.rag.controller")  // 指定扫描Controller的包
@OpenAPIDefinition(info = @Info(title = "RAG API", version = "v1", description = "RAG系统的API文档"))
public class SwaggerConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/api/swagger-ui.html/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}

