// DocumentResponse.java  
package com.luanjining.rag.dto.response;

public class DocumentResponse {
    private String docId;
    private String spaceId;

    public DocumentResponse() {}
    public DocumentResponse(String docId, String spaceId) {
        this.docId = docId;
        this.spaceId = spaceId;
    }
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
}