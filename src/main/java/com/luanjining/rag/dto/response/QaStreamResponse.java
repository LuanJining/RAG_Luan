
// QaStreamResponse.java
package com.luanjining.rag.dto.response;

import java.util.List;

public class QaStreamResponse {
    private String delta;
    private Boolean finish;
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

    public static class Reference {
        private Long docId;
        private String title;
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