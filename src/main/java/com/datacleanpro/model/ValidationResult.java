package com.datacleanpro.model;

import java.time.LocalDateTime;

/**
 * 验证结果模型类
 * 表示一个数据验证结果
 */
public class ValidationResult {
    private Long id;
    private Long fileId;
    private Long ruleId;
    private int rowIndex;
    private String columnName;
    private String cellValue;
    private boolean passed;
    private String errorMessage;
    private LocalDateTime validatedAt;

    public ValidationResult() {
        this.validatedAt = LocalDateTime.now();
    }

    public ValidationResult(Long fileId, Long ruleId, int rowIndex, String columnName, String cellValue, boolean passed, String errorMessage) {
        this.fileId = fileId;
        this.ruleId = ruleId;
        this.rowIndex = rowIndex;
        this.columnName = columnName;
        this.cellValue = cellValue;
        this.passed = passed;
        this.errorMessage = errorMessage;
        this.validatedAt = LocalDateTime.now();
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

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getCellValue() {
        return cellValue;
    }

    public void setCellValue(String cellValue) {
        this.cellValue = cellValue;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", ruleId=" + ruleId +
                ", rowIndex=" + rowIndex +
                ", columnName='" + columnName + '\'' +
                ", passed=" + passed +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
