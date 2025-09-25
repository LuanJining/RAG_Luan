// SpaceResponse.java
package com.luanjining.rag.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SpaceResponse {
    private String spaceId;

    public SpaceResponse() {}
    public SpaceResponse(String spaceId) { this.spaceId = spaceId; }

}
