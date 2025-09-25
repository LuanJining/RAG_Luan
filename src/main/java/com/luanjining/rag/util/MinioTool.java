package com.luanjining.rag.util;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * MinioTool
 * 演示：
 * - connectMinio: 连接本地 MinIO
 * - uploadFileWithMetadata: 上传文件并附加元数据
 * - getFileInfo: 查询文件信息和元数据
 * - updateFile: 更新文件内容或元数据
 * - deleteFile: 删除文件
 */
@Component
public class MinioTool {
    private static final Logger logger = LoggerFactory.getLogger(MinioTool.class);

    @Value("${rag.minio.endpoint}")
    private String ENDPOINT;

    @Value("${rag.minio.access-key}")
    private String ACCESS_KEY;

    @Value("${rag.minio.secret-key}")
    private String SECRET_KEY;

    @Value("${rag.minio.bucket-name}")
    private String BUCKET_NAME;

    private static MinioClient client;


    /** 连接本地 MinIO */
    public MinioTool() {
        client = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
        System.out.println("✅ 已连接到 MinIO: " + ENDPOINT);
    }

    /** 创建Bucket */
    public boolean createBucket(String name, String description) throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("description", description);
        boolean found = client.bucketExists(
                BucketExistsArgs.builder().bucket(name).build());

        if (!found) {
            client.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            client.setBucketTags(
                    SetBucketTagsArgs.builder()
                            .bucket(name)
                            .tags(tags)
                            .build()
            );
            System.out.println("✅ Bucket 创建成功: " + name);
            return true;
        } else {
            System.out.println("ℹ️ Bucket 已存在: " + name);
            return false;
        }
    }


    /** 上传文件并添加自定义元数据 */
    public void uploadFileWithMetadata(String objectName, String filePath, String datasetId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        // 将所属知识库信息存到对象元数据中
        headers.put("datasetId", datasetId);

        try (InputStream is = new FileInputStream(filePath)) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(is, -1, 10 * 1024 * 1024)
                            .headers(headers)
                            .build()
            );
        }
        System.out.println("✅ 上传完成:  (元数据: datasetId=" + datasetId + ")");
    }


    /** 查询文件信息与元数据 */
    public void getFileInfo(String objectName) throws Exception {
        StatObjectResponse stat = client.statObject(
                StatObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .build()
        );
        System.out.println("ℹ️ 文件信息: ");
        System.out.println("  名称: " + objectName);
        System.out.println("  大小: " + stat.size() + " bytes");
        System.out.println("  内容类型: " + stat.contentType());
        System.out.println("  自定义元数据: " + stat.userMetadata());
    }

    /** 更新文件内容或元数据（覆盖同名对象） */
    public void updateFile(String objectName, String newFilePath,
                           String datasetId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("datasetId", datasetId);

        try (InputStream is = new FileInputStream(newFilePath)) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(is, -1, 10 * 1024 * 1024)
                            .headers(headers)
                            .build()
            );
        }
        System.out.println("✅ 已更新对象: " + objectName + " (新元数据: ");
    }

    /** 删除文件 */
    public void deleteFile(String objectName) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .build()
        );
        System.out.println("🗑️ 已删除对象: " + objectName);
    }
}


