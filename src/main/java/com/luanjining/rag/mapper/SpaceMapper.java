package com.luanjining.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luanjining.rag.entity.Space;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识空间Mapper接口
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {

    /**
     * 根据名称查询空间
     */
    @Select("SELECT * FROM spaces WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Space> findByNameLike(@Param("name") String name);

    /**
     * 查询所有空间
     */
    @Select("SELECT * FROM spaces ORDER BY create_time DESC")
    List<Space> findAllOrderByCreateTime();
}