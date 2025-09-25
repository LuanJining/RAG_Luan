package com.luanjining.rag.controller;

import com.luanjining.rag.dto.request.QaRequest;
import com.luanjining.rag.service.QaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 问答控制器 - 实现API文档规范
 */
@RestController
@RequestMapping("/v1/spaces")
public class QaController {

    private static final Logger logger = LoggerFactory.getLogger(QaController.class);

    @Autowired
    private QaService qaService;

    /**
     * 知识库问答（流式返回）
     * POST /api/v1/spaces/{spaceId}/qa/stream
     * 请求: {"query": "特种设备维保周期？", "userId": 123}
     * 响应: SSE流式 text/event-stream
     */
    @PostMapping(value = "/{spaceId}/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQa(@PathVariable Long spaceId, @RequestBody QaRequest request) {

        logger.info("收到知识库问答请求: spaceId={}, query={}, userId={}",
                spaceId, request.getQuery(), request.getUserId());

        if (spaceId == null) {
            return qaService.createErrorEmitter("知识空间ID不能为空");
        }

        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return qaService.createErrorEmitter("查询内容不能为空");
        }

        if (request.getUserId() == null) {
            return qaService.createErrorEmitter("用户ID不能为空");
        }

        return qaService.streamAnswer(spaceId, request.getQuery(), request.getUserId());
    }
}