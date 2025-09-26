package com.luanjining.rag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件映射实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_map")
public class FileMap {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 空间ID
     */
    private String spaceId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String extension;


}