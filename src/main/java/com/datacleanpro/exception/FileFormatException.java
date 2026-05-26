package com.datacleanpro.exception;

/**
 * 文件格式异常类
 * 当文件格式不正确或无法解析时抛出
 */
public class FileFormatException extends DataCleanException {
    private String filePath;
    private String expectedFormat;
    private String actualFormat;

    public FileFormatException(String message) {
        super("FILE_FORMAT_ERROR", message);
    }

    public FileFormatException(String message, Throwable cause) {
        super("FILE_FORMAT_ERROR", message, cause);
    }

    public FileFormatException(String filePath, String expectedFormat, String actualFormat) {
        super("FILE_FORMAT_ERROR", 
              String.format("文件格式不正确: 期望 %s, 实际 %s, 文件: %s", 
                          expectedFormat, actualFormat, filePath));
        this.filePath = filePath;
        this.expectedFormat = expectedFormat;
        this.actualFormat = actualFormat;
    }

    public FileFormatException(String message, String filePath, String expectedFormat, String actualFormat) {
        super("FILE_FORMAT_ERROR", message);
        this.filePath = filePath;
        this.expectedFormat = expectedFormat;
        this.actualFormat = actualFormat;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getExpectedFormat() {
        return expectedFormat;
    }

    public void setExpectedFormat(String expectedFormat) {
        this.expectedFormat = expectedFormat;
    }

    public String getActualFormat() {
        return actualFormat;
    }

    public void setActualFormat(String actualFormat) {
        this.actualFormat = actualFormat;
    }

    @Override
    public String toString() {
        return "FileFormatException{" +
                "filePath='" + filePath + '\'' +
                ", expectedFormat='" + expectedFormat + '\'' +
                ", actualFormat='" + actualFormat + '\'' +
                ", errorCode='" + getErrorCode() + '\'' +
                ", errorMessage='" + getErrorMessage() + '\'' +
                '}';
    }
}
