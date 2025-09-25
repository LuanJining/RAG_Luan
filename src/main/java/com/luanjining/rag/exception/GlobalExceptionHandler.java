package com.luanjining.rag.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RagException.class)
    public ResponseEntity<Map<String, Object>> handleRagException(RagException e) {
        logger.error("RAG业务异常: {}", e.getMessage(), e);
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("code", e.getErrorCode());
        return ResponseEntity.ok(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        logger.error("文件上传大小超限: {}", e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "文件大小超出限制(最大10MB)");
        return ResponseEntity.ok(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("参数错误: {}", e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.ok(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("系统异常: {}", e.getMessage(), e);
        Map<String, Object> error = new HashMap<>();
        error.put("error", "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}