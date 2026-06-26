package com.datacleanpro.model;

import java.util.List;
import java.util.ArrayList;

/**
 * 查询条件模型类
 * 表示数据查询条件
 */
public class QueryCondition {
    private Long fileId;
    private List<String> columns;
    private String whereClause;
    private String orderBy;
    private int limit;
    private int offset;

    public QueryCondition() {
        this.columns = new ArrayList<>();
        this.limit = 100;
        this.offset = 0;
    }

    public QueryCondition(Long fileId) {
        this.fileId = fileId;
        this.columns = new ArrayList<>();
        this.limit = 100;
        this.offset = 0;
    }

    // Getters and Setters
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "QueryCondition{" +
                "fileId=" + fileId +
                ", columns=" + columns +
                ", whereClause='" + whereClause + '\'' +
                ", orderBy='" + orderBy + '\'' +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
