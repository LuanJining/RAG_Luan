package com.luanjining.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luanjining.rag.entity.FileMap;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文件映射Mapper接口
 */
@Mapper
public interface FileMapMapper extends BaseMapper<FileMap> {

    /**
     * 根据空间ID查询文件
     */
    @Select("SELECT * FROM file_map WHERE space_id = #{spaceId} ORDER BY create_time DESC")
    List<FileMap> findBySpaceId(@Param("spaceId") String spaceId);

    /**
     * 根据文件名模糊查询
     */
    @Select("SELECT * FROM file_map WHERE space_id = #{spaceId} AND file_name LIKE CONCAT('%', #{fileName}, '%')")
    List<FileMap> findBySpaceIdAndFileNameLike(@Param("spaceId") String spaceId, @Param("fileName") String fileName);

    /**
     * 更新文件信息
     */
    @Update("UPDATE file_map SET file_name = #{fileName}, description = #{description}, update_time = NOW() WHERE document_id = #{documentId}")
    int updateFileInfo(@Param("documentId") String documentId, @Param("fileName") String fileName, @Param("description") String description);

    /**
     * 根据documentId查询文件映射
     */
    @Select("SELECT * FROM file_map WHERE document_id = #{docId}")
    FileMap findByDocumentId(@Param("docId") String docId);

    /**
     * 根据documentId删除文件映射
     */
    @Delete("DELETE FROM file_map WHERE document_id = #{docId}")
    int deleteByDocumentId(@Param("docId") String docId);


}