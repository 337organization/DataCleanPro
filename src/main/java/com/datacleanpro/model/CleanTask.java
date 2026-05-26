package com.datacleanpro.model;

import java.time.LocalDateTime;

/**
 * 清洗任务模型类
 * 表示一个数据清洗任务
 */
public class CleanTask {
    private Long id;
    private Long fileId;
    private String taskType;
    private String status;
    private int rowsAffected;
    private String detail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    public CleanTask() {
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public CleanTask(Long fileId, String taskType) {
        this.fileId = fileId;
        this.taskType = taskType;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRowsAffected() {
        return rowsAffected;
    }

    public void setRowsAffected(int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void start() {
        this.status = "RUNNING";
        this.startTime = LocalDateTime.now();
    }

    public void complete(int rowsAffected) {
        this.status = "COMPLETED";
        this.rowsAffected = rowsAffected;
        this.endTime = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = "FAILED";
        this.detail = errorMessage;
        this.endTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "CleanTask{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", rowsAffected=" + rowsAffected +
                '}';
    }
}
