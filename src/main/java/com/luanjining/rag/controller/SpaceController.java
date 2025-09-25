package com.luanjining.rag.controller;

import com.luanjining.rag.dto.request.CreateSpaceRequest;
import com.luanjining.rag.dto.response.SpaceResponse;
import com.luanjining.rag.service.SpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 知识空间控制器 - 实现API文档规范
 */
@RestController
@RequestMapping("/v1/spaces")
public class SpaceController {

    private static final Logger logger = LoggerFactory.getLogger(SpaceController.class);

    @Autowired
    private SpaceService spaceService;

    /**
     * 创建知识空间
     * POST /api/v1/spaces
     * 请求: {"name": "研发部知识库", "description": "研发制度与规范"}
     * 响应: {"spaceId": 1}
     */
    @PostMapping
    public SpaceResponse createSpace(@RequestBody CreateSpaceRequest request) {

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