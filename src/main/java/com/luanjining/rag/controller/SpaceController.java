package com.luanjining.rag.controller;

import com.luanjining.rag.dto.request.CreateSpaceRequest;
import com.luanjining.rag.dto.response.SpaceResponse;
import com.luanjining.rag.service.SpaceService;
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

/**
 * 知识空间控制器 - 实现API文档规范
 */
@Tag(name = "知识空间管理", description = "知识空间的创建和管理")
@RestController
@RequestMapping("/v1/spaces")
public class SpaceController {

    private static final Logger logger = LoggerFactory.getLogger(SpaceController.class);

    @Autowired
    private SpaceService spaceService;

    /**
     * 创建知识空间
     * POST /api/v1/spaces
     * 请求: {"name": name, "description": description}
     * 响应: {"spaceId": spaceId}
     */
    @Operation(
            summary = "创建知识空间",
            description = "创建一个新的知识空间，用于组织和管理相关文档。" +
                    "系统会自动生成基于时间戳的唯一空间ID。"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "知识空间创建成功",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SpaceResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"知识空间名称不能为空\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"数据库插入失败\"}")
                    )
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SpaceResponse createSpace(
            @Parameter(
                    description = "创建知识空间的请求信息",
                    required = true
            )
            @RequestBody CreateSpaceRequest request) {

        logger.info("收到创建知识空间请求: name={}, description={}",
                request.getName(), request.getDescription());

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("知识空间名称不能为空");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("知识空间描述不能为空");
        }

        return spaceService.createSpace(request.getName(), request.getDescription());
    }
}