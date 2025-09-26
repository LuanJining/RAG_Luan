package com.luanjining.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties
@MapperScan("com.luanjining.rag.mapper")
@EnableTransactionManagement
public class RagApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagApplication.class, args);
		System.out.println("========================================");
		System.out.println("🚀 RAG Service 启动成功!");
		System.out.println("📚 API基础地址: http://localhost:8085/api");
		System.out.println("💓 健康检查: http://localhost:8085/api/health");
		System.out.println("🗄️ 数据库已连接: PostgreSQL kb_platform");
		System.out.println("🔧 MybatisPlus已启用");
		System.out.println("========================================");
	}
}