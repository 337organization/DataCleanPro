package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;

import java.util.List;

/**
 * 数据清洗器接口
 * 定义数据清洗的通用接口
 */
public interface DataCleaner {
    
    /**
     * 清洗数据
     * @param data 数据行列表
     * @return 清洗结果
     */
    CleanResult clean(List<DataRow> data);
    
    /**
     * 获取清洗器名称
     * @return 清洗器名称
     */
    String getName();
    
    /**
     * 获取清洗器描述
     * @return 清洗器描述
     */
    String getDescription();
    
    /**
     * 清洗结果内部类
     */
    class CleanResult {
        private int totalRows;
        private int affectedRows;
        private int removedRows;
        private String detail;
        
        public CleanResult() {}
        
        public CleanResult(int totalRows, int affectedRows, int removedRows, String detail) {
            this.totalRows = totalRows;
            this.affectedRows = affectedRows;
            this.removedRows = removedRows;
            this.detail = detail;
        }
        
        // Getters and Setters
        public int getTotalRows() {
            return totalRows;
        }
        
        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }
        
        public int getAffectedRows() {
            return affectedRows;
        }
        
        public void setAffectedRows(int affectedRows) {
            this.affectedRows = affectedRows;
        }
        
        public int getRemovedRows() {
            return removedRows;
        }
        
        public void setRemovedRows(int removedRows) {
            this.removedRows = removedRows;
        }
        
        public String getDetail() {
            return detail;
        }
        
        public void setDetail(String detail) {
            this.detail = detail;
        }
        
        /**
         * 合并另一个清洗结果
         * @param other 另一个清洗结果
         */
        public void merge(CleanResult other) {
            if (other != null) {
                this.totalRows = Math.max(this.totalRows, other.totalRows);
                this.affectedRows += other.affectedRows;
                this.removedRows += other.removedRows;
                if (this.detail == null) {
                    this.detail = other.detail;
                } else if (other.detail != null) {
                    this.detail += "; " + other.detail;
                }
            }
        }
        
        @Override
        public String toString() {
            return "CleanResult{" +
                    "totalRows=" + totalRows +
                    ", affectedRows=" + affectedRows +
                    ", removedRows=" + removedRows +
                    ", detail='" + detail + '\'' +
                    '}';
        }
    }
}
