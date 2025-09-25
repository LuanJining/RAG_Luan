package com.luanjining.rag.service;

import com.luanjining.rag.config.RagConfiguration;
import com.luanjining.rag.dto.response.QaStreamResponse;
import com.luanjining.rag.exception.RagException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 问答服务 - 从API_demo.chatLLM迁移并扩展为SSE流式
 */
@Service
public class QaService {

    private static final Logger logger = LoggerFactory.getLogger(QaService.class);

    @Autowired
    private RagConfiguration ragConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 流式问答 - 从API_demo.chatLLM扩展为SSE
     */
    public SseEmitter streamAnswer(Long spaceId, String query, Long userId) {
        SseEmitter emitter = new SseEmitter(60000L);

        executorService.execute(() -> {
            try {
                logger.info("开始流式问答: spaceId={}, query={}, userId={}", spaceId, query, userId);

                String requestBody = String.format("{\n" +
                        "  \"inputs\": {},\n" +
                        "  \"response_mode\": \"streaming\",\n" +
                        "  \"auto_generate_name\": true,\n" +
                        "  \"query\": \"%s\",\n" +
                        "  \"user\": \"%s\"\n" +
                        "}", escapeJson(query), ragConfig.getUserId());

                HttpResponse<String> response = Unirest.post(ragConfig.getBaseUrl() + "/chat-messages")
                        .header("Authorization", ragConfig.getAuthorizationApp())
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .asString();

                if (response.getStatus() == 200) {
                    simulateStreamingResponse(emitter, response.getBody());
                } else {
                    logger.error("问答请求失败: status={}, body={}", response.getStatus(), response.getBody());
                    emitter.completeWithError(new RagException("问答失败: " + response.getBody()));
                }

            } catch (Exception e) {
                logger.error("流式问答异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public SseEmitter createErrorEmitter(String errorMessage) {
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\": \"" + errorMessage + "\"}"));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 模拟流式响应 - 符合API文档的SSE格式
     */
    private void simulateStreamingResponse(SseEmitter emitter, String fullResponse) {
        try {
            String[] segments = {
                    "根据《安全管理规范》",
                    "特种设备维保周期为6个月",
                    "具体包括以下几个方面：",
                    "1. 定期检查设备运行状态",
                    "2. 清洁和润滑关键部件",
                    "3. 更换易损件",
                    "4. 记录维保过程和结果"
            };

            for (String segment : segments) {
                QaStreamResponse deltaResponse = new QaStreamResponse(segment);
                emitter.send(SseEmitter.event()
                        .name("data")
                        .data(objectMapper.writeValueAsString(deltaResponse)));

                Thread.sleep(200);
            }

            List<QaStreamResponse.Reference> references = new ArrayList<>();
            references.add(new QaStreamResponse.Reference(101L, "安全管理规范", "/files/101.pdf"));

            QaStreamResponse finishResponse = new QaStreamResponse(true, references);
            emitter.send(SseEmitter.event()
                    .name("data")
                    .data(objectMapper.writeValueAsString(finishResponse)));

            emitter.complete();
            logger.info("流式问答完成");

        } catch (Exception e) {
            logger.error("发送流式响应异常", e);
            emitter.completeWithError(e);
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}