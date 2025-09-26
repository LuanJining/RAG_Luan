// DocumentResponse.java  
package com.luanjining.rag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "文档创建响应")
public class DocumentResponse {
    @Schema(description = "文档ID", example = "doc-12345")
    private String docId;
    @Schema(description = "知识空间ID", example = "20241201120000")
    private String spaceId;

    public DocumentResponse() {}
    public DocumentResponse(String docId, String spaceId) {
        this.docId = docId;
        this.spaceId = spaceId;
    }

}