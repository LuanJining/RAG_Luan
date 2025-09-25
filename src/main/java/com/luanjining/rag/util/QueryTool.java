
// ===== 2. QueryTool.java =====
package com.luanjining.rag.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 查询工具类
 * 提供文档查询相关功能
 */
@Component
public class QueryTool {

    private static final Logger logger = LoggerFactory.getLogger(QueryTool.class);

    @Value("${rag.dify.authorization-dataset}")
    private String authorizationDataset;

    @Value("${rag.dify.base-url}")
    private String baseUrl;

    @Value("${rag.dify.dataset-id}")
    private String datasetId;

    /**
     * 查询文档
     * 若keyword为空则查询全部，默认分页，每页20条
     *
     * @param keyword 关键词，可为空
     * @return HTTP响应
     */
    public HttpResponse<String> queryDocument(String keyword) {
        return queryDocument(keyword, 1, 20);
    }

    /**
     * 查询文档（支持分页）
     *
     * @param keyword 关键词，可为空
     * @param page 页码，从1开始
     * @param limit 每页条数，最大100
     * @return HTTP响应
     */
    public HttpResponse<String> queryDocument(String keyword, int page, int limit) {
        // 参数验证
        if (page < 1) page = 1;
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;

        String url = buildQueryUrl(keyword, page, limit);
        logger.debug("查询文档URL: {}", url);

        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Authorization", authorizationDataset)
                    .header("Accept", "application/json")
                    .asString();

            logger.debug("查询响应状态: {}", response.getStatus());

            return response;
        } catch (Exception e) {
            logger.error("文档查询请求失败", e);
            throw new RuntimeException("文档查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文档ID查询特定文档
     *
     * @param documentId 文档ID
     * @return HTTP响应
     */
    public HttpResponse<String> queryDocumentById(String documentId) {
        if (documentId == null || documentId.trim().isEmpty()) {
            throw new IllegalArgumentException("文档ID不能为空");
        }

        String url = baseUrl + "/datasets/" + datasetId + "/documents/" + documentId;
        logger.debug("查询特定文档URL: {}", url);

        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Authorization", authorizationDataset)
                    .header("Accept", "application/json")
                    .asString();

            logger.debug("查询特定文档响应状态: {}", response.getStatus());

            return response;
        } catch (Exception e) {
            logger.error("查询特定文档失败: documentId={}", documentId, e);
            throw new RuntimeException("查询特定文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询数据集信息
     *
     * @return HTTP响应
     */
    public HttpResponse<String> queryDatasetInfo() {
        String url = baseUrl + "/datasets/" + datasetId;
        logger.debug("查询数据集信息URL: {}", url);

        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Authorization", authorizationDataset)
                    .header("Accept", "application/json")
                    .asString();

            logger.debug("查询数据集信息响应状态: {}", response.getStatus());

            return response;
        } catch (Exception e) {
            logger.error("查询数据集信息失败", e);
            throw new RuntimeException("查询数据集信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有数据集列表
     *
     * @return HTTP响应
     */
    public HttpResponse<String> listDatasets() {
        String url = baseUrl + "/datasets";
        logger.debug("获取数据集列表URL: {}", url);

        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Authorization", authorizationDataset)
                    .header("Accept", "application/json")
                    .asString();

            logger.debug("获取数据集列表响应状态: {}", response.getStatus());

            return response;
        } catch (Exception e) {
            logger.error("获取数据集列表失败", e);
            throw new RuntimeException("获取数据集列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建查询URL
     *
     * @param keyword 关键词
     * @param page 页码
     * @param limit 每页条数
     * @return 完整的查询URL
     */
    private String buildQueryUrl(String keyword, int page, int limit) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("/datasets/").append(datasetId).append("/documents");
        url.append("?page=").append(page);
        url.append("&limit=").append(limit);

        if (keyword != null && !keyword.trim().isEmpty()) {
            // URL编码关键词
            try {
                String encodedKeyword = java.net.URLEncoder.encode(keyword.trim(), "UTF-8");
                url.append("&keyword=").append(encodedKeyword);
            } catch (Exception e) {
                logger.warn("关键词URL编码失败: {}", keyword, e);
                url.append("&keyword=").append(keyword.trim());
            }
        }

        return url.toString();
    }

    /**
     * 检查服务连接状态
     *
     * @return true if connected, false otherwise
     */
    public boolean checkConnection() {
        try {
            HttpResponse<String> response = queryDatasetInfo();
            return response.getStatus() >= 200 && response.getStatus() < 300;
        } catch (Exception e) {
            logger.warn("连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取配置信息（用于调试）
     *
     * @return 配置信息字符串
     */
    public String getConfigInfo() {
        return String.format("baseUrl: %s, datasetId: %s, authToken: %s",
                baseUrl,
                datasetId,
                authorizationDataset != null ? "已配置" : "未配置");
    }
}