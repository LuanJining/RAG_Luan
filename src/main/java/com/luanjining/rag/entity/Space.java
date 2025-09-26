package com.luanjining.rag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 知识空间实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spaces")
public class Space {

    /**
     * 空间ID
     */
    private String spaceId;

    /**
     * 空间描述
     */
    private String description;

    /**
     * 空间名称
     */
    private String name;


}