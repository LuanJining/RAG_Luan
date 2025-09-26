package com.luanjining.rag.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "创建知识空间请求")
public class CreateSpaceRequest {
    @Schema(description = "空间名称", required = true, example = "企业安全管理知识库")
    private String name;
    @Schema(description = "空间描述", required = true, example = "包含企业安全管理相关的规范、制度和操作指南")
    private String description;

    public CreateSpaceRequest() {}

    public CreateSpaceRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

}