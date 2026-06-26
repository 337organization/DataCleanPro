package com.datacleanpro.exception;

/**
 * 数据清洗系统基础异常类
 * 所有自定义异常的父类
 */
public class DataCleanException extends RuntimeException {
    private String errorCode;
    private String errorMessage;

    public DataCleanException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public DataCleanException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public DataCleanException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public DataCleanException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "DataCleanException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
