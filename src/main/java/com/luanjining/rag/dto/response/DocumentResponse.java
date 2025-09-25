// DocumentResponse.java  
package com.luanjining.rag.dto.response;

public class DocumentResponse {
    private Long docId;
    private Long spaceId;

    public DocumentResponse() {}
    public DocumentResponse(Long docId, Long spaceId) {
        this.docId = docId;
        this.spaceId = spaceId;
    }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
}