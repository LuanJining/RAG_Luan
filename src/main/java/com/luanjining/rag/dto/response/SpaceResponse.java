package com.luanjining.rag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "知识空间创建响应")
public class SpaceResponse {
    @Schema(description = "空间ID", example = "20241201120000")
    private String spaceId;

    public SpaceResponse() {}
    public SpaceResponse(String spaceId) { this.spaceId = spaceId; }
}