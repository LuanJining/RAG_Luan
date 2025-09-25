package com.luanjining.rag.controller;

import com.luanjining.rag.dto.response.DocumentResponse;
import com.luanjining.rag.dto.response.SearchResponse;
import com.luanjining.rag.dto.response.SuccessResponse;
import com.luanjining.rag.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档控制器 - 实现API文档规范
 */
@RestController
@RequestMapping("/v1/spaces")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    /**
     * 创建文档（上传PDF/Word）
     * POST /api/v1/spaces/{spaceId}/docs
     * 响应: {"docId": 101, "spaceId": 1}
     */
    @PostMapping(value = "/{spaceId}/docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse createDocument(
            @PathVariable Long spaceId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {

        logger.info("收到创建文档请求: spaceId={}, title={}, fileName={}",
                spaceId, title, file.getOriginalFilename());

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("文档标题不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".pdf") &&
                !fileName.toLowerCase().endsWith(".docx"))) {
            throw new IllegalArgumentException("仅支持PDF和DOCX格式");
        }

        return documentService.createDocument(spaceId, title, file);
    }

    /**
     * 编辑文档
     * PUT /api/v1/spaces/{spaceId}/docs/{docId}
     * 响应: {"success": true}
     */
    @PutMapping(value = "/{spaceId}/docs/{docId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse updateDocument(
            @PathVariable Long spaceId,
            @PathVariable Long docId,
            @RequestParam(required = false) String title,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        logger.info("收到编辑文档请求: spaceId={}, docId={}, title={}",
                spaceId, docId, title);

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (docId == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }

        if ((title == null || title.trim().isEmpty()) && (file == null || file.isEmpty())) {
            throw new IllegalArgumentException("标题和文件至少要更新一个");
        }

        return documentService.updateDocument(spaceId, docId, title, file);
    }

    /**
     * 删除文档
     * DELETE /api/v1/spaces/{spaceId}/docs/{docId}?userId=123
     * 响应: {"success": true}
     */
    @DeleteMapping("/{spaceId}/docs/{docId}")
    public SuccessResponse deleteDocument(
            @PathVariable Long spaceId,
            @PathVariable Long docId,
            @RequestParam Long userId) {

        logger.info("收到删除文档请求: spaceId={}, docId={}, userId={}",
                spaceId, docId, userId);

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (docId == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return documentService.deleteDocument(spaceId, docId, userId);
    }

    /**
     * 搜索文档
     * GET /api/v1/spaces/{spaceId}/docs/search?q=安全管理&userId=123
     * 响应: {"items": [...]}
     */
    @GetMapping("/{spaceId}/docs/search")
    public SearchResponse searchDocuments(
            @PathVariable Long spaceId,
            @RequestParam String q,
            @RequestParam Long userId) {

        logger.info("收到搜索文档请求: spaceId={}, q={}, userId={}",
                spaceId, q, userId);

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (q == null || q.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return documentService.searchDocuments(spaceId, q, userId);
    }
}