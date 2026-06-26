package com.datacleanpro.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据文件模型类
 * 表示导入的文件元数据
 */
public class DataFile {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private int rowCount;
    private int columnCount;
    private LocalDateTime importTime;
    private String status;
    private String description;
    private List<String> headers;

    public DataFile() {
        this.importTime = LocalDateTime.now();
        this.status = "IMPORTED";
    }

    public DataFile(String fileName, String filePath, String fileType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.importTime = LocalDateTime.now();
        this.status = "IMPORTED";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public LocalDateTime getImportTime() {
        return importTime;
    }

    public void setImportTime(LocalDateTime importTime) {
        this.importTime = importTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
        if (headers != null) {
            this.columnCount = headers.size();
        }
    }

    @Override
    public String toString() {
        return "DataFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", rowCount=" + rowCount +
                ", columnCount=" + columnCount +
                ", status='" + status + '\'' +
                '}';
    }
}
