
// SearchResponse.java
package com.luanjining.rag.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SearchResponse {
    private List<SearchItem> items;

    public SearchResponse() {}
    public SearchResponse(List<SearchItem> items) { this.items = items; }

    @Setter
    @Getter
    public static class SearchItem {
        // Getters and Setters
        private String docId;
        private String title;

        public SearchItem() {}
        public SearchItem(String docId, String title) {
            this.docId = docId;
            this.title = title;
        }

    }
}