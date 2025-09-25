package com.luanjining.rag.service;

import com.luanjining.rag.config.RagConfiguration;
import com.luanjining.rag.dto.response.DocumentResponse;
import com.luanjining.rag.dto.response.SearchResponse;
import com.luanjining.rag.dto.response.SuccessResponse;
import com.luanjining.rag.exception.RagException;
import com.luanjining.rag.util.FileTextExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档服务 - 从API_demo.java迁移文档相关业务逻辑
 */
@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private RagConfiguration ragConfig;

    @Autowired
    private FileTextExtractor fileTextExtractor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建文档 - 从API_demo.create_document迁移
     */
    public DocumentResponse createDocument(Long spaceId, String title, MultipartFile file) {
        try {
            logger.info("开始创建文档: spaceId={}, title={}, fileName={}",
                    spaceId, title, file.getOriginalFilename());

            File tempFile = convertMultipartFileToFile(file);
            String extractedText = fileTextExtractor.getExtractedText(tempFile);

            HttpResponse<String> response = Unirest.post(ragConfig.getBaseUrl() + "/datasets/" +
                            ragConfig.getDatasetId() + "/document/create-by-text")
                    .header("Authorization", ragConfig.getAuthorizationDataset())
                    .header("Content-Type", "application/json")
                    .body(buildCreateDocumentJson(title, extractedText))
                    .asString();

            tempFile.delete();

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                logger.info("文档创建成功: {}", response.getBody());

                try {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    String documentId = jsonNode.path("document").path("id").asText();
                    Long docId = Math.abs((long) documentId.hashCode());
                    return new DocumentResponse(docId, spaceId);
                } catch (Exception e) {
                    logger.warn("解析文档ID失败，返回默认ID: {}", e.getMessage());
                    return new DocumentResponse(101L, spaceId);
                }

            } else {
                logger.error("文档创建失败: status={}, body={}", response.getStatus(), response.getBody());
                throw new RagException("创建文档失败: " + response.getBody(), "CREATE_DOCUMENT_FAILED");
            }

        } catch (Exception e) {
            logger.error("创建文档异常", e);
            throw new RagException("创建文档时发生错误: " + e.getMessage(), "CREATE_DOCUMENT_ERROR");
        }
    }

    /**
     * 更新文档 - 从API_demo.update_document迁移
     */
    public SuccessResponse updateDocument(Long spaceId, Long docId, String title, MultipartFile file) {
        try {
            logger.info("开始更新文档: spaceId={}, docId={}, title={}", spaceId, docId, title);

            String difyDocumentId = "991f797e-bed4-4b66-86e4-c76edf97fe4b";

            String extractedText = "";
            if (file != null && !file.isEmpty()) {
                File tempFile = convertMultipartFileToFile(file);
                extractedText = fileTextExtractor.getExtractedText(tempFile);
                tempFile.delete();
            }

            HttpResponse<String> response = Unirest.post(ragConfig.getBaseUrl() + "/datasets/" +
                            ragConfig.getDatasetId() + "/documents/" +
                            difyDocumentId + "/update-by-text")
                    .header("Authorization", ragConfig.getAuthorizationDataset())
                    .header("Content-Type", "application/json")
                    .body(buildUpdateDocumentJson(title, extractedText))
                    .asString();

            if (response.getStatus() == 200) {
                logger.info("文档更新成功: {}", response.getBody());
                return new SuccessResponse(true);
            } else {
                logger.error("文档更新失败: status={}, body={}", response.getStatus(), response.getBody());
                throw new RagException("更新文档失败: " + response.getBody(), "UPDATE_DOCUMENT_FAILED");
            }

        } catch (Exception e) {
            logger.error("更新文档异常", e);
            throw new RagException("更新文档时发生错误: " + e.getMessage(), "UPDATE_DOCUMENT_ERROR");
        }
    }

    /**
     * 删除文档 - 从API_demo.delete_document迁移
     */
    public SuccessResponse deleteDocument(Long spaceId, Long docId, Long userId) {
        try {
            logger.info("开始删除文档: spaceId={}, docId={}, userId={}", spaceId, docId, userId);

            String difyDocumentId = "991f797e-bed4-4b66-86e4-c76edf97fe4b";

            HttpResponse<String> response = Unirest.delete(ragConfig.getBaseUrl() + "/datasets/" +
                            ragConfig.getDatasetId() + "/documents/" +
                            difyDocumentId)
                    .header("Authorization", ragConfig.getAuthorizationDataset())
                    .asString();

            if (response.getStatus() == 200) {
                logger.info("文档删除成功: {}", response.getBody());
                return new SuccessResponse(true);
            } else {
                logger.error("文档删除失败: status={}, body={}", response.getStatus(), response.getBody());
                throw new RagException("删除文档失败: " + response.getBody(), "DELETE_DOCUMENT_FAILED");
            }

        } catch (Exception e) {
            logger.error("删除文档异常", e);
            throw new RagException("删除文档时发生错误: " + e.getMessage(), "DELETE_DOCUMENT_ERROR");
        }
    }

    /**
     * 搜索文档
     */
    public SearchResponse searchDocuments(Long spaceId, String query, Long userId) {
        try {
            logger.info("开始搜索文档: spaceId={}, query={}, userId={}", spaceId, query, userId);

            String url = ragConfig.getBaseUrl() + "/datasets/" + ragConfig.getDatasetId() +
                    "/documents?page=1&limit=20&keyword=" + query;

            HttpResponse<String> response = Unirest.get(url)
                    .header("Authorization", ragConfig.getAuthorizationDataset())
                    .asString();

            if (response.getStatus() == 200) {
                logger.info("文档搜索成功");

                try {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    JsonNode dataArray = jsonNode.path("data");

                    List<SearchResponse.SearchItem> items = new ArrayList<>();
                    for (JsonNode item : dataArray) {
                        String name = item.path("name").asText();
                        String documentId = item.path("id").asText();

                        String snippet = "……解析后的文本片段……";
                        String fileUrl = "/files/" + documentId + ".pdf";

                        Long docId = Math.abs((long) documentId.hashCode());
                        items.add(new SearchResponse.SearchItem(docId, name, snippet, fileUrl));
                    }

                    return new SearchResponse(items);
                } catch (Exception e) {
                    logger.warn("解析搜索结果失败: {}", e.getMessage());
                    List<SearchResponse.SearchItem> items = new ArrayList<>();
                    items.add(new SearchResponse.SearchItem(101L, "安全管理规范",
                            "……解析后的文本片段……", "/files/101.pdf"));
                    return new SearchResponse(items);
                }

            } else {
                logger.error("文档搜索失败: status={}, body={}", response.getStatus(), response.getBody());
                throw new RagException("搜索文档失败: " + response.getBody(), "SEARCH_DOCUMENTS_FAILED");
            }

        } catch (Exception e) {
            logger.error("搜索文档异常", e);
            throw new RagException("搜索文档时发生错误: " + e.getMessage(), "SEARCH_DOCUMENTS_ERROR");
        }
    }

    // 辅助方法
    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload_", "_" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    private String buildCreateDocumentJson(String title, String text) {
        return "{\n" +
                "  \"name\": \"" + escapeJson(title) + "\",\n" +
                "  \"text\": \"" + escapeJson(text) + "\",\n" +
                "  \"indexing_technique\": \"high_quality\",\n" +
                "  \"doc_form\": \"text_model\",\n" +
                "  \"doc_language\": \"中文\",\n" +
                "  \"process_rule\": {\n" +
                "    \"mode\": \"automatic\",\n" +
                "    \"rules\": {\n" +
                "      \"pre_processing_rules\": [\n" +
                "        {\n" +
                "          \"id\": \"remove_extra_spaces\",\n" +
                "          \"enabled\": true\n" +
                "        }\n" +
                "      ],\n" +
                "      \"segmentation\": {\n" +
                "        \"separator\": \"###\",\n" +
                "        \"max_tokens\": 500\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"retrieval_model\": {\n" +
                "    \"search_method\": \"hybrid_search\",\n" +
                "    \"reranking_enable\": true,\n" +
                "    \"top_k\": 5,\n" +
                "    \"score_threshold_enabled\": true,\n" +
                "    \"score_threshold\": 0.8,\n" +
                "    \"weights\":{\n" +
                "      \"semantic\": 0.5,\n" +
                "      \"keyword\": 0.5\n" +
                "    }\n" +
                "  },\n" +
                "  \"embedding_model\": \"text-embedding-ada-002\",\n" +
                "  \"embedding_model_provider\": \"openai\"\n" +
                "}";
    }

    private String buildUpdateDocumentJson(String title, String text) {
        StringBuilder json = new StringBuilder("{\n");

        if (title != null && !title.trim().isEmpty()) {
            json.append("  \"name\": \"").append(escapeJson(title)).append("\",\n");
        }

        if (text != null && !text.trim().isEmpty()) {
            json.append("  \"text\": \"").append(escapeJson(text)).append("\",\n");
        }

        json.append("  \"process_rule\": {\n")
                .append("    \"mode\": \"automatic\",\n")
                .append("    \"rules\": {\n")
                .append("      \"pre_processing_rules\": [\n")
                .append("        {\n")
                .append("          \"id\": \"remove_extra_spaces\",\n")
                .append("          \"enabled\": true\n")
                .append("        }\n")
                .append("      ],\n")
                .append("      \"segmentation\": {\n")
                .append("        \"separator\": \"###\",\n")
                .append("        \"max_tokens\": 500\n")
                .append("      }\n")
                .append("    }\n")
                .append("  }\n")
                .append("}");

        return json.toString();
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