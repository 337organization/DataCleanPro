package com.datacleanpro.network;

import java.io.Serializable;

/**
 * 任务响应类
 * 网络通信的响应对象
 */
public class TaskResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String requestId;
    private boolean success;
    private String message;
    private Object data;
    private int progress;
    private long timestamp;
    
    public TaskResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public TaskResponse(String requestId, boolean success, String message) {
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "TaskResponse{" +
                "requestId='" + requestId + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", progress=" + progress +
                ", timestamp=" + timestamp +
                '}';
    }
}
