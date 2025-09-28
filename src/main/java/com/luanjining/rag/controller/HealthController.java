package com.luanjining.rag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Tag(name = "系统监控", description = "系统健康检查和状态监控")
@RestController
@RequestMapping("/health")
public class HealthController {

    @Operation(
            summary = "健康检查",
            description = "检查服务运行状态，返回服务基本信息和运行状态"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "服务运行正常",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    example = "{\"status\":\"UP\",\"timestamp\":\"2024-12-01T12:00:00\",\"service\":\"RAG-Service\",\"version\":\"1.0.0\"}"
                            )
                    )
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "RAG-Service");
        healthInfo.put("version", "1.0.0");

        return healthInfo;
    }
}