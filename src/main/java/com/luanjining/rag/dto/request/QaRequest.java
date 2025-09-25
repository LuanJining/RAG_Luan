package com.luanjining.rag.dto.request;

public class QaRequest {
    private String query;
    private Long userId;

    public QaRequest() {}

    public QaRequest(String query, Long userId) {
        this.query = query;
        this.userId = userId;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}