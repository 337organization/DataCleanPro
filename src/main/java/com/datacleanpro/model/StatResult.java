package com.datacleanpro.model;

/**
 * 统计结果模型类
 * 表示数据统计结果
 */
public class StatResult {
    private String columnName;
    private long totalCount;
    private long nullCount;
    private long duplicateCount;
    private long errorCount;
    private long validCount;
    private String minValue;
    private String maxValue;
    private Double avgValue;

    public StatResult() {
    }

    public StatResult(String columnName) {
        this.columnName = columnName;
    }

    // Getters and Setters
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getNullCount() {
        return nullCount;
    }

    public void setNullCount(long nullCount) {
        this.nullCount = nullCount;
    }

    public long getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(long duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getValidCount() {
        return validCount;
    }

    public void setValidCount(long validCount) {
        this.validCount = validCount;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public Double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    @Override
    public String toString() {
        return "StatResult{" +
                "columnName='" + columnName + '\'' +
                ", totalCount=" + totalCount +
                ", nullCount=" + nullCount +
                ", duplicateCount=" + duplicateCount +
                ", errorCount=" + errorCount +
                ", validCount=" + validCount +
                '}';
    }
}
