package com.luanjining.rag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "文档搜索响应")
public class SearchResponse {
    @Schema(description = "搜索结果列表")
    private List<SearchItem> items;

    public SearchResponse() {}
    public SearchResponse(List<SearchItem> items) { this.items = items; }

    @Setter
    @Getter
    @Schema(description = "搜索结果项")
    public static class SearchItem {
        @Schema(description = "文档ID", example = "doc-12345")
        private String docId;

        @Schema(description = "文档标题", example = "安全管理规范")
        private String title;

        public SearchItem() {}
        public SearchItem(String docId, String title) {
            this.docId = docId;
            this.title = title;
        }
    }
}