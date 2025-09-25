package com.luanjining.rag.service;

import com.luanjining.rag.dto.response.SpaceResponse;
import com.luanjining.rag.exception.RagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class SpaceService {

    private static final Logger logger = LoggerFactory.getLogger(SpaceService.class);


    /**
     * 创建知识空间
     */
    public SpaceResponse createSpace(String name, String description) {
            logger.info("开始创建知识空间: name={}, description={}", name, description);

            //利用当前时间生成唯一编号
            String spaceId = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());

//            //将知识空间信息传入postgres数据库
//            if (spaceMapper.insert(new Space(name, spaceId, description)) == 0) {
//                throw new RagException("数据库插入知识空间失败", "DB_INSERT_SPACE_FAILED");
//            } else {
//                logger.info("知识空间信息已保存到数据库");
//                return new SpaceResponse(spaceId);
//            }
        return new SpaceResponse(spaceId);
    }

}