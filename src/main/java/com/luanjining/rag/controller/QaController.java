package com.luanjining.rag.controller;

import com.luanjining.rag.service.QaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import kong.unirest.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 请求: {"query": "特种设备维保周期？", "userId": 123}
     * 响应: SSE流式 text/event-stream
     */

    // TODO: 这里改为POST更合适
    //       响应改为SSE流式
    @GetMapping(value = "/{spaceId}/qa/stream")
    public HttpResponse<String> streamQa(@PathVariable String spaceId, @RequestParam String q) {

        logger.info("收到知识库问答请求: spaceId={}, query={}",
                    spaceId, q);

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (q == null || q.trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }


        return qaService.streamAnswer(spaceId, q);
    }
}