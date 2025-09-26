package com.luanjining.rag.service;

import com.luanjining.rag.config.RagConfiguration;
import com.luanjining.rag.dto.response.DocumentResponse;
import com.luanjining.rag.dto.response.SearchResponse;
import com.luanjining.rag.dto.response.SuccessResponse;
import com.luanjining.rag.entity.FileMap;
import com.luanjining.rag.exception.RagException;
import com.luanjining.rag.mapper.FileMapMapper;
import com.luanjining.rag.mapper.SpaceMapper;
import com.luanjining.rag.util.FileTextExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
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
    private FileMapMapper fileMapMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private FileTextExtractor fileTextExtractor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建文档
     */
    public DocumentResponse createDocument(String spaceId, String title, MultipartFile file) {
        File tempFile = null;
        try {
            logger.info("开始创建文档: spaceId={}, title={}, fileName={}", spaceId, title, file.getOriginalFilename());

            // 1. 先上传到MinIO（在文件还未被清理时）
            boolean is_success = minioService.uploadFile(file, title);
            if (!is_success) {
                throw new RagException("MinIO上传失败", "MINIO_UPLOAD_FAILED");
            }
            logger.info("MinIO上传成功");

            // 2. 处理文件提取文本
            tempFile = convertMultipartFileToFile(file);
            String extractedText = fileTextExtractor.getExtractedText(tempFile);

            // 3. 将文件内容发送到RAG平台创建文档
            HttpResponse<String> response = Unirest.post(ragConfig.getBaseUrl() + "/datasets/" +
                            ragConfig.getDatasetId() + "/document/create-by-text")
                    .header("Authorization", ragConfig.getAuthorizationDataset())
                    .header("Content-Type", "application/json")
                    .body(buildCreateDocumentJson(title, extractedText))
                    .asString();

            if (response.getStatus() != 200 && response.getStatus() != 201) {
                logger.error("Dify API调用失败: status={}, body={}", response.getStatus(), response.getBody());
                throw new RagException("创建文档失败: " + response.getBody(), "CREATE_DOCUMENT_FAILED");
            }

            // 4. 将docId从response的请求体中提取出来
            String body = response.getBody();
            JSONObject json = new JSONObject(body);
            String docId = json.getJSONObject("document").getString("id");

            // 获取原文件扩展名
            String extension = "";
            if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
                extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }

            // 5. postgres操作
            FileMap fileMap = new FileMap();
            fileMap.setDocumentId(docId);
            fileMap.setFileName(title);
            fileMap.setSpaceId(spaceId);
            fileMap.setExtension(extension);

            int result = fileMapMapper.insert(fileMap);
            if (result <= 0) {
                throw new RagException("数据库插入失败", "DB_INSERT_FAILED");
            }

            logger.info("文档创建成功: docId={}", docId);
            return new DocumentResponse(docId, spaceId);

        } catch (Exception e) {
            logger.error("创建文档异常", e);
            throw new RagException("创建文档时发生错误: " + e.getMessage(), "CREATE_DOCUMENT_ERROR");
        } finally {
            // 6. 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                logger.info("临时文件清理: {} - {}", tempFile.getAbsolutePath(), deleted ? "成功" : "失败");
            }
        }
    }

    /**
     * 更新文档
     */
    public SuccessResponse updateDocument(String spaceId, String docId, String title, MultipartFile file) {
        File tempFile = null;
        try {
            logger.info("开始更新文档: spaceId={}, docId={}, title={}", spaceId, docId, title);

            // 1. 先上传到MinIO（在文件还未被清理时）
            boolean is_success = minioService.uploadFile(file, title);
            if (!is_success) {
                throw new RagException("MinIO上传失败", "MINIO_UPLOAD_FAILED");
            }

            // 2. 处理文件提取文本
            tempFile = convertMultipartFileToFile(file);
            String extractedText = fileTextExtractor.getExtractedText(tempFile);

            // 3. 将更新内容发送到RAG平台
            HttpResponse<String> response = Unirest.post(ragConfig.getBaseUrl() + "/datasets/" +
                            ragConfig.getDatasetId() + "/documents/" +
                            docId + "/update-by-text")
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
    public SuccessResponse deleteDocument(String spaceId, String docId) {
        try {
            logger.info("开始删除文档: spaceId={}, docId={}", spaceId, docId);

            // 先从数据库中查出对应的文件名
            FileMap fileMap = fileMapMapper.findByDocumentId(docId);

            //删除数据库中的文件映射记录
            fileMapMapper.deleteByDocumentId(docId);

            // 从MinIO删除
            boolean is_success = minioService.deleteFile(fileMap.getFileName()+fileMap.getExtension());
            if (!is_success) {
                throw new RagException("MinIO删除失败", "MINIO_UPLOAD_FAILED");
            }
            logger.info("MinIO删除成功");

            // 将文件从RAG平台删除
            HttpResponse<String> response =Unirest.delete(ragConfig.getBaseUrl() + "/datasets/"+ragConfig.getDatasetId()+"/documents/"+docId)
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
        public SearchResponse searchDocuments(String spaceId, String fileName) {
        try {
            logger.info("开始搜索文档: spaceId={}, fileName={}", spaceId, fileName);
            SearchResponse searchResponse = new SearchResponse();
            List<SearchResponse.SearchItem> items = new ArrayList<>();

            List<FileMap> fileMaps = fileMapMapper.findBySpaceIdAndFileNameLike(spaceId, fileName);
            for (FileMap fileMap : fileMaps) {
                SearchResponse.SearchItem item = new SearchResponse.SearchItem();
                item.setDocId(fileMap.getDocumentId());
                item.setTitle(fileMap.getFileName());
                items.add(item);
            }
            searchResponse.setItems(items);

            return searchResponse;

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