package com.luanjining.rag.service;

import com.luanjining.rag.dto.response.SpaceResponse;
import com.luanjining.rag.entity.Space;
import com.luanjining.rag.exception.RagException;
import com.luanjining.rag.mapper.SpaceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class SpaceService {

    private static final Logger logger = LoggerFactory.getLogger(SpaceService.class);

    @Autowired
    private SpaceMapper spaceMapper;

    /**
     * 创建知识空间
     */
    public SpaceResponse createSpace(String name, String description) {
            logger.info("开始创建知识空间: name={}, description={}", name, description);

            //利用当前时间生成唯一编号
            String spaceId = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());

            Space space = new Space();
            space.setDescription(description);
            space.setName(name);
            space.setSpaceId(spaceId);

            if (spaceMapper.insert(space) > 0) {
                logger.info("知识空间信息已保存到数据库");
                return new SpaceResponse(spaceId);
            } else {
                throw new RagException("数据库插入知识空间失败", "DB_INSERT_SPACE_FAILED");
            }

    }

}