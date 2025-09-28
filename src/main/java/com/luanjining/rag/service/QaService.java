package com.luanjining.rag.service;

import com.luanjining.rag.config.RagConfiguration;
import com.luanjining.rag.dto.response.QaStreamResponse;
import com.luanjining.rag.exception.RagException;
import com.luanjining.rag.mapper.FileMapMapper;
import com.luanjining.rag.entity.FileMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 问答服务 - 从API_demo.chatLLM迁移并扩展为SSE流式
 */
@Service
public class QaService {

    private static final Logger logger = LoggerFactory.getLogger(QaService.class);

    @Autowired
    private RagConfiguration ragConfig;

    @Autowired
    private FileMapMapper fileMapMapper;

    @Autowired
    private MinioService minioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 流式问答 - 返回SSE流
     */
    public SseEmitter streamAnswer(String spaceId, String query) {
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时

        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("开始流式问答: spaceId={}, query={}", spaceId, query);

                // 调用Dify API获取流式响应
                HttpResponse<String> difyResponse = Unirest.post(ragConfig.getBaseUrl() + "/chat-messages")
                        .header("Authorization", ragConfig.getAuthorizationApp())
                        .header("Content-Type", "application/json")
                        .body(buildChatRequestJson(query))
                        .asString();

                if (difyResponse.getStatus() != 200) {
                    logger.error("Dify API调用失败: status={}, body={}", difyResponse.getStatus(), difyResponse.getBody());
                    throw new RagException("Dify API调用失败: " + difyResponse.getBody());
                }

                // 解析并转发流式响应
                parseAndForwardStreamResponse(difyResponse.getBody(), emitter, spaceId);

            } catch (Exception e) {
                logger.error("流式问答异常", e);
                try {
                    // 发送错误信息
                    QaStreamResponse errorResponse = new QaStreamResponse("系统错误: " + e.getMessage());
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(errorResponse)));
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // 如果发送错误信息也失败，直接完成
                }
            }
        });

        return emitter;
    }

    /**
     * 解析并转发流式响应数据
     */
    private void parseAndForwardStreamResponse(String responseBody, SseEmitter emitter, String spaceId) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(responseBody));
            String line;
            List<QaStreamResponse.Reference> references = new ArrayList<>();
            boolean hasContent = false;
            StringBuilder textBuffer = new StringBuilder(); // 用于累积文本

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6).trim(); // 去掉 "data: " 前缀

                    // 跳过空数据和结束标记
                    if (jsonData.isEmpty() || jsonData.equals("[DONE]")) {
                        continue;
                    }

                    try {
                        JsonNode json = objectMapper.readTree(jsonData);

                        // 处理文本增量数据 - Dify通常使用 "delta" 字段
                        if (json.has("delta")) {
                            String deltaText = json.get("delta").asText();
                            if (!deltaText.isEmpty()) {
                                textBuffer.append(deltaText);

                                // 当累积足够的文本或遇到标点符号时发送
                                if (shouldSendBuffer(textBuffer.toString())) {
                                    QaStreamResponse deltaResponse = new QaStreamResponse(textBuffer.toString());
                                    String deltaJson = objectMapper.writeValueAsString(deltaResponse);
                                    emitter.send(SseEmitter.event().data(deltaJson));
                                    hasContent = true;
                                    logger.debug("发送增量数据: {}", textBuffer.toString());
                                    textBuffer.setLength(0); // 清空缓冲区
                                }
                            }
                        }

                        // 也尝试处理 "answer" 字段（备用）
                        if (json.has("answer")) {
                            String answerText = json.get("answer").asText();
                            if (!answerText.isEmpty()) {
                                QaStreamResponse deltaResponse = new QaStreamResponse(answerText);
                                String deltaJson = objectMapper.writeValueAsString(deltaResponse);
                                emitter.send(SseEmitter.event().data(deltaJson));
                                hasContent = true;
                                logger.debug("发送完整答案: {}", answerText);
                            }
                        }

                        // 处理引用信息
                        if (json.has("metadata") && json.get("metadata").has("retriever_resources")) {
                            JsonNode resources = json.get("metadata").get("retriever_resources");
                            for (JsonNode resource : resources) {
                                if (resource.has("document_id") && resource.has("document_name")) {
                                    String docId = resource.get("document_id").asText();
                                    String title = resource.get("document_name").asText();

                                    // 查询文件映射并生成URL
                                    String fileUrl = generateFileUrl(docId);

                                    // 尝试将docId转换为Long，如果失败则使用hashCode
                                    Long docIdLong = (long) docId.hashCode();
                                    try {
                                        docIdLong = Long.parseLong(docId);
                                    } catch (NumberFormatException e) {
                                        logger.debug("文档ID非数字，使用hashCode: {}", docId);
                                    }

                                    QaStreamResponse.Reference ref = new QaStreamResponse.Reference(
                                            docIdLong, title, fileUrl);
                                    references.add(ref);
                                }
                            }
                        }

                    } catch (Exception e) {
                        logger.warn("解析Dify响应JSON失败: {}", jsonData, e);
                    }
                }
            }

            // 发送剩余的缓冲区内容
            if (textBuffer.length() > 0) {
                QaStreamResponse deltaResponse = new QaStreamResponse(textBuffer.toString());
                String deltaJson = objectMapper.writeValueAsString(deltaResponse);
                emitter.send(SseEmitter.event().data(deltaJson));
                hasContent = true;
            }

            // 如果没有收到任何内容，发送默认消息
            if (!hasContent) {
                QaStreamResponse defaultResponse = new QaStreamResponse("抱歉，暂时无法找到相关信息。");
                String defaultJson = objectMapper.writeValueAsString(defaultResponse);
                emitter.send(SseEmitter.event().data(defaultJson));
            }

            // 添加模拟引用（如果没有找到引用）
            if (references.isEmpty()) {
                // 从数据库查询该空间下的文档作为可能的引用
                List<FileMap> spaceFiles = fileMapMapper.findBySpaceId(spaceId);
                for (FileMap fileMap : spaceFiles.subList(0, Math.min(2, spaceFiles.size()))) { // 最多取2个
                    String fileUrl = generateFileUrl(fileMap.getDocumentId());
                    QaStreamResponse.Reference ref = new QaStreamResponse.Reference(
                            (long) fileMap.getDocumentId().hashCode(),
                            fileMap.getFileName(),
                            fileUrl);
                    references.add(ref);
                }
            }

            // 发送完成信号和引用信息
            QaStreamResponse finishResponse = new QaStreamResponse(true, references);
            String finishJson = objectMapper.writeValueAsString(finishResponse);
            emitter.send(SseEmitter.event().data(finishJson));
            emitter.complete();

            logger.info("流式响应处理完成，共发送{}个引用", references.size());

        } catch (IOException e) {
            logger.error("解析流式响应失败", e);
            try {
                QaStreamResponse errorResponse = new QaStreamResponse("解析响应失败: " + e.getMessage());
                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(errorResponse)));
                emitter.completeWithError(e);
            } catch (Exception ignored) {
                // 发送错误信息失败，直接完成
            }
        }
    }

    /**
     * 判断是否应该发送缓冲区内容
     */
    private boolean shouldSendBuffer(String buffer) {
        // 当遇到句号、问号、感叹号、换行或者长度超过10个字符时发送
        return buffer.length() > 10 ||
                buffer.endsWith("。") ||
                buffer.endsWith("？") ||
                buffer.endsWith("！") ||
                buffer.endsWith("\n") ||
                buffer.endsWith("，") ||
                buffer.endsWith("；");
    }

    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String docId) {
        try {
            FileMap fileMap = fileMapMapper.findByDocumentId(docId);
            if (fileMap != null) {
                String fileName = fileMap.getFileName() + fileMap.getExtension();
                return minioService.getFileUrl(fileName, 3600); // 1小时有效期
            }
        } catch (Exception e) {
            logger.warn("生成文件URL失败: docId={}", docId, e);
        }
        return "/files/" + docId + ".pdf"; // 默认路径
    }

    /**
     * 构建聊天请求JSON
     */
    private String buildChatRequestJson(String query) {
        return "{\n" +
                "  \"inputs\": {},\n" +
                "  \"response_mode\": \"streaming\",\n" +
                "  \"auto_generate_name\": true,\n" +
                "  \"query\": \"" + escapeJson(query) + "\",\n" +
                "  \"user\": \"" + ragConfig.getUserId() + "\"\n" +
                "}";
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}