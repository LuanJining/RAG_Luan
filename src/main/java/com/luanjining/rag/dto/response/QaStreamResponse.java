package com.luanjining.rag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "问答流式响应")
public class QaStreamResponse {
    @Schema(description = "增量文本内容", example = "根据安全管理规范...")
    private String delta;

    @Schema(description = "是否完成", example = "true")
    private Boolean finish;

    @Schema(description = "参考文档列表")
    private List<Reference> references;

    public QaStreamResponse() {}
    public QaStreamResponse(String delta) { this.delta = delta; }
    public QaStreamResponse(Boolean finish, List<Reference> references) {
        this.finish = finish;
        this.references = references;
    }

    // Getters and Setters
    public String getDelta() { return delta; }
    public void setDelta(String delta) { this.delta = delta; }
    public Boolean getFinish() { return finish; }
    public void setFinish(Boolean finish) { this.finish = finish; }
    public List<Reference> getReferences() { return references; }
    public void setReferences(List<Reference> references) { this.references = references; }

    @Schema(description = "参考文档信息")
    public static class Reference {
        @Schema(description = "文档ID", example = "123")
        private Long docId;

        @Schema(description = "文档标题", example = "安全管理规范")
        private String title;

        @Schema(description = "文件访问URL", example = "/files/doc123.pdf")
        private String fileUrl;

        public Reference() {}
        public Reference(Long docId, String title, String fileUrl) {
            this.docId = docId;
            this.title = title;
            this.fileUrl = fileUrl;
        }

        // Getters and Setters
        public Long getDocId() { return docId; }
        public void setDocId(Long docId) { this.docId = docId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    }
}