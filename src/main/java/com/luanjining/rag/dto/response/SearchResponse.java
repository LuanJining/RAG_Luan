
// SearchResponse.java
package com.luanjining.rag.dto.response;

import java.util.List;

public class SearchResponse {
    private List<SearchItem> items;

    public SearchResponse() {}
    public SearchResponse(List<SearchItem> items) { this.items = items; }
    public List<SearchItem> getItems() { return items; }
    public void setItems(List<SearchItem> items) { this.items = items; }

    public static class SearchItem {
        private Long docId;
        private String title;
        private String snippet;
        private String fileUrl;

        public SearchItem() {}
        public SearchItem(Long docId, String title, String snippet, String fileUrl) {
            this.docId = docId;
            this.title = title;
            this.snippet = snippet;
            this.fileUrl = fileUrl;
        }

        // Getters and Setters
        public Long getDocId() { return docId; }
        public void setDocId(Long docId) { this.docId = docId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    }
}