package com.datacleanpro.model;

import java.time.LocalDateTime;

/**
 * 任务历史模型类
 * 表示任务历史记录
 */
public class TaskHistory {
    private Long id;
    private Long taskId;
    private String action;
    private String target;
    private String detail;
    private String status;
    private String errorMessage;
    private Long executionTime;
    private LocalDateTime createdAt;

    public TaskHistory() {
        this.status = "SUCCESS";
        this.createdAt = LocalDateTime.now();
    }

    public TaskHistory(String action, String target) {
        this.action = action;
        this.target = target;
        this.status = "SUCCESS";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void success() {
        this.status = "SUCCESS";
    }

    public void fail(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "TaskHistory{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", target='" + target + '\'' +
                ", status='" + status + '\'' +
                ", executionTime=" + executionTime +
                '}';
    }
}
