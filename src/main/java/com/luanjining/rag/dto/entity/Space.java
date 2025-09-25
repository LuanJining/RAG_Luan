package com.luanjining.rag.dto.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("spaces")
public class Space {

    @TableField("space_id")
    private String spaceId;

    @TableField("description")
    private String description;

    @TableField("name")
    private String name;

    public Space(String name, String spaceId, String description) {
        this.spaceId = spaceId;
        this.description = description;
        this.name = name;
    }
}
