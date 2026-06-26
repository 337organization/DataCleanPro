package com.datacleanpro.model;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * 数据行模型类
 * 表示导入文件中的一行数据
 */
public class DataRow {
    private Long id;
    private Long fileId;
    private int rowIndex;
    private List<String> fields;
    private boolean deleted;
    private LocalDateTime createdAt;

    public DataRow() {
        this.fields = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public DataRow(Long fileId, int rowIndex, List<String> fields) {
        this.fileId = fileId;
        this.rowIndex = rowIndex;
        this.fields = fields != null ? fields : new ArrayList<>();
        this.deleted = false;
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

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getField(int index) {
        if (index >= 0 && index < fields.size()) {
            return fields.get(index);
        }
        return null;
    }

    public void setField(int index, String value) {
        while (fields.size() <= index) {
            fields.add("");
        }
        fields.set(index, value);
    }

    public int getFieldCount() {
        return fields.size();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "DataRow{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", rowIndex=" + rowIndex +
                ", fields=" + fields +
                ", deleted=" + deleted +
                '}';
    }
}
