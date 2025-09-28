package com.luanjining.rag.controller;

import com.luanjining.rag.service.QaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 问答控制器 - 实现API文档规范
 */
@Tag(name = "问答服务", description = "基于知识库的问答功能")
@RestController
@RequestMapping("/v1/spaces")
public class QaController {

    private static final Logger logger = LoggerFactory.getLogger(QaController.class);

    @Autowired
    private QaService qaService;

    /**
     * 知识库问答（流式返回）
     * POST /api/v1/spaces/{spaceId}/qa/stream
     * 请求: {"query": "特种设备维保周期？"}
     * 响应: SSE流式 text/event-stream
     */
    @Operation(
            summary = "知识库问答流式接口",
            description = "基于知识库进行问答，返回SSE流式数据。" +
                    "响应格式为Server-Sent Events，包含增量文本和参考文档信息。"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "问答流开始",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    example = "data: {\"delta\":\"根据安全管理规范...\"}\n\n" +
                                            "data: {\"finish\":true,\"references\":[{\"docId\":123,\"title\":\"安全管理规范\",\"fileUrl\":\"/files/doc123.pdf\"}]}\n\n"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"查询内容不能为空\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"系统内部错误\"}")
                    )
            )
    })
    @PostMapping(value = "/{spaceId}/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQa(
            @Parameter(
                    description = "知识空间ID",
                    required = true,
                    example = "20241201120000"
            )
            @PathVariable String spaceId,

            @Parameter(
                    description = "问答请求体",
                    required = true
            )
            @RequestBody QaRequest request) {

        logger.info("收到知识库问答请求: spaceId={}, query={}",
                spaceId, request.getQuery());

        if (spaceId == null || spaceId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }

        return qaService.streamAnswer(spaceId, request.getQuery());
    }

    /**
     * 问答请求DTO
     */
    @Schema(description = "问答请求")
    public static class QaRequest {
        @Schema(description = "查询问题", required = true, example = "特种设备的维保周期是多久？")
        private String query;

        @Schema(description = "用户ID（可选）", required = false, example = "user123")
        private String userId;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}