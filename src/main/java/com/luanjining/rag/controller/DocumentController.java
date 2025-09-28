package com.luanjining.rag.controller;

import com.luanjining.rag.dto.response.DocumentResponse;
import com.luanjining.rag.dto.response.SearchResponse;
import com.luanjining.rag.dto.response.SuccessResponse;
import com.luanjining.rag.service.DocumentService;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档控制器 - 实现API文档规范
 */
@RestController
@RequestMapping("/v1/spaces")
@Tag(name = "文档管理", description = "文档相关的增删改查操作")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Operation(
            summary = "创建文档",
            description = "上传PDF或Word文档到指定知识空间。支持的文件格式：PDF、DOCX"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "文档创建成功",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DocumentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"文件格式不支持\"}")
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
    @PostMapping(value = "/{spaceId}/docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse createDocument(
            @Parameter(
                    description = "知识空间ID",
                    required = true,
                    example = "20241201120000",
                    schema = @Schema(type = "string", pattern = "\\d{14}")
            )
            @PathVariable String spaceId,

            @Parameter(
                    description = "文档标题",
                    required = true,
                    example = "安全管理规范",
                    schema = @Schema(type = "string", maxLength = 100)
            )
            @RequestParam String title,

            @Parameter(
                    description = "文档文件(支持PDF、DOCX格式，最大10MB)",
                    required = true
            )
            @RequestParam("file") MultipartFile file) {

        logger.info("收到创建文档请求: spaceId={}, title={}, fileName={}",
                spaceId, title, file.getOriginalFilename());

        if (spaceId == null || spaceId.trim().isEmpty()) {
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

    @Operation(
            summary = "编辑文档",
            description = "更新文档标题或上传新文件。可以只更新标题，或只更新文件，或同时更新"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "文档编辑成功",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"标题和文件至少要更新一个\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "文档不存在",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"文档不存在\"}")
                    )
            )
    })
    @PutMapping(value = "/{spaceId}/docs/{docId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse updateDocument(
            @Parameter(
                    description = "知识空间ID",
                    required = true,
                    example = "20241201120000"
            )
            @PathVariable String spaceId,

            @Parameter(
                    description = "文档ID",
                    required = true,
                    example = "doc-12345"
            )
            @PathVariable String docId,

            @Parameter(
                    description = "新的文档标题（可选）",
                    required = false,
                    example = "安全管理规范(更新版)"
            )
            @RequestParam(required = false) String title,

            @Parameter(
                    description = "新的文档文件（可选）",
                    required = false
            )
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

    @Operation(
            summary = "删除文档",
            description = "根据文档ID删除文档，包括从知识库和文件存储中彻底删除"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "文档删除成功",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "文档不存在",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"文档不存在\"}")
                    )
            )
    })
    @DeleteMapping("/{spaceId}/docs/{docId}")
    public SuccessResponse deleteDocument(
            @Parameter(
                    description = "知识空间ID",
                    required = true,
                    example = "20241201120000"
            )
            @PathVariable String spaceId,

            @Parameter(
                    description = "文档ID",
                    required = true,
                    example = "doc-12345"
            )
            @PathVariable String docId) {

        logger.info("收到删除文档请求: spaceId={}, docId={}", spaceId, docId);

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        if (docId == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }

        return documentService.deleteDocument(spaceId, docId);
    }

    @Operation(
            summary = "搜索文档",
            description = "根据查询关键词在指定知识空间中搜索文档。如果不提供关键词，则返回该空间下的所有文档"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "搜索成功",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SearchResponse.class)
                    )
            )
    })
    @GetMapping("/{spaceId}/docs/search")
    public SearchResponse searchDocuments(
            @Parameter(
                    description = "知识空间ID",
                    required = true,
                    example = "20241201120000"
            )
            @PathVariable String spaceId,

            @Parameter(
                    description = "搜索关键词（可选，不提供则返回所有文档）",
                    required = false,
                    example = "安全管理"
            )
            @RequestParam(required = false) String q) {

        logger.info("收到搜索文档请求: spaceId={}, q={}", spaceId, q);

        if (spaceId == null) {
            throw new IllegalArgumentException("知识空间ID不能为空");
        }

        return documentService.searchDocuments(spaceId, q);
    }
}