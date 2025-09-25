package com.luanjining.rag.dto.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("file_map")
public class FileMap {
    @TableField("document_id")
    private String documentId;

    @TableField("space_id")
    private String spaceId;

    @TableField("file_name")
    private String fileName;

    @TableField("description")
    private String description;

    public FileMap(String documentId, String fileName, String spaceId, String description) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.spaceId = spaceId;
        this.description = description;
    }
}
