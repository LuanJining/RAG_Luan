package com.luanjining.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@MapperScan("com.luanjining.rag.Mapper") // 添加Mapper扫描
public class RagApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagApplication.class, args);
		System.out.println("========================================");
		System.out.println("🚀 RAG Service 启动成功!");
		System.out.println("📚 API基础地址: http://localhost:8085/api");
		System.out.println("💓 健康检查: http://localhost:8085/api/health");
		System.out.println("========================================");
	}
}