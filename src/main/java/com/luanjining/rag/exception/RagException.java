package com.luanjining.rag.exception;

public class RagException extends RuntimeException {
    private String errorCode;

    public RagException(String message) {
        super(message);
    }

    public RagException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RagException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}