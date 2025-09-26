package com.luanjining.rag.service;

import com.luanjining.rag.exception.RagException;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MinIO文件存储服务
 */
@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @PostConstruct
    public void initMinioClient() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        System.out.println("✅ 已连接到 MinIO: " + endpoint);
    }


    /**
     * 上传文件
     * @param file 要上传的文件
     * @param title 自定义文件名
     * @return 文件存储路径
     */
    public boolean uploadFile(MultipartFile file, String title) {
        try {

            // 检查参数
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("标题不能为空");
            }

            // 获取原文件扩展名
            String extension = "";
            if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
                extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }

            // 生成对象名：title.扩展名
            String objectName = title + extension;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );


            logger.info("文件上传成功: {} -> {}", file.getOriginalFilename(), objectName);
            return true;

        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }


    /**
         * 下载文件
         * @param objectName 对象名称
         * @return 文件输入流
         */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("文件下载失败: {}", objectName, e);
            throw new RagException("文件下载失败: " + e.getMessage(), "MINIO_DOWNLOAD_ERROR");
        }
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public boolean deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("文件删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            logger.error("文件删除失败: {}", objectName, e);
            throw new RagException("文件删除失败: " + e.getMessage(), "MINIO_DELETE_ERROR");
        }
    }

    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 预签名URL
     */
    public String getFileUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );
        } catch (Exception e) {
            logger.error("获取文件URL失败: {}", objectName, e);
            throw new RagException("获取文件URL失败: " + e.getMessage(), "MINIO_URL_ERROR");
        }
    }



    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 列出指定知识空间的所有文件
     * @param spaceId 知识空间ID
     * @return 文件列表
     */
    public List<String> listFiles(Long spaceId) {
        List<String> files = new ArrayList<>();
        try {
            String prefix = "spaces/" + spaceId + "/";
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                files.add(item.objectName());
            }
        } catch (Exception e) {
            logger.error("列出文件失败: spaceId={}", spaceId, e);
            throw new RagException("列出文件失败: " + e.getMessage(), "MINIO_LIST_ERROR");
        }
        return files;
    }




    /**
     * 获取存储桶信息
     */
    public String getBucketInfo() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            StringBuilder info = new StringBuilder();
            info.append("MinIO连接成功\n");
            info.append("服务地址: ").append(endpoint).append("\n");
            info.append("当前存储桶: ").append(bucketName).append("\n");
            info.append("所有存储桶: ");
            for (Bucket bucket : buckets) {
                info.append(bucket.name()).append(" ");
            }
            return info.toString();
        } catch (Exception e) {
            logger.error("获取存储桶信息失败", e);
            return "MinIO连接失败: " + e.getMessage();
        }
    }
}