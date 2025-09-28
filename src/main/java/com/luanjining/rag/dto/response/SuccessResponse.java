package com.luanjining.rag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "操作成功响应")
public class SuccessResponse {
    @Schema(description = "操作是否成功", example = "true")
    private boolean success;

    public SuccessResponse() {}
    public SuccessResponse(boolean success) { this.success = success; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}