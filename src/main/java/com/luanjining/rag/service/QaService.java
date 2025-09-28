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


    /**
     * 流式问答 - 从API_demo.chatLLM扩展为SSE
     */
    public HttpResponse<String> streamAnswer(String spaceId, String query) {

        try {
            logger.info("开始流式问答: spaceId={}, query={}", spaceId, query);

            return Unirest.post(ragConfig.getBaseUrl() + "/chat-messages")
                    .header("Authorization", ragConfig.getAuthorizationApp())
                    .header("Content-Type", "application/json")
                    .body("{\n  \"inputs\": {},\n  \"response_mode\": \"streaming\",\n  \"auto_generate_name\": true,\n  \"query\": \""+query+"\",\n  \"user\": \""+ ragConfig.getUserId() +"\"\n}")
                    .asString();

        } catch (Exception e) {
            logger.error("流式问答异常", e);
            throw e;
        }

    }

}