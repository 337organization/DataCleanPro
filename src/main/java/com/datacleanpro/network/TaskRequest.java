package com.datacleanpro.network;

import java.io.Serializable;

/**
 * 任务请求类
 * 网络通信的请求对象
 */
public class TaskRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String requestId;
    private String action;
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private String parameters;
    private long timestamp;
    
    public TaskRequest() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public TaskRequest(String action) {
        this.action = action;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public byte[] getFileData() {
        return fileData;
    }
    
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "TaskRequest{" +
                "requestId='" + requestId + '\'' +
                ", action='" + action + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
