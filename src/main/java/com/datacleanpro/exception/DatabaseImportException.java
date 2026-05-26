package com.datacleanpro.exception;

/**
 * 数据库导入异常类
 * 当数据库导入操作失败时抛出
 */
public class DatabaseImportException extends DataCleanException {
    private String tableName;
    private int rowIndex;
    private String sqlState;

    public DatabaseImportException(String message) {
        super("DB_IMPORT_ERROR", message);
    }

    public DatabaseImportException(String message, Throwable cause) {
        super("DB_IMPORT_ERROR", message, cause);
    }

    public DatabaseImportException(String tableName, int rowIndex, String message) {
        super("DB_IMPORT_ERROR", 
              String.format("数据库导入失败: 表 '%s', 行 %d, 原因: %s", 
                          tableName, rowIndex, message));
        this.tableName = tableName;
        this.rowIndex = rowIndex;
    }

    public DatabaseImportException(String message, String tableName, int rowIndex, String sqlState) {
        super("DB_IMPORT_ERROR", message);
        this.tableName = tableName;
        this.rowIndex = rowIndex;
        this.sqlState = sqlState;
    }

    public DatabaseImportException(String message, Throwable cause, String tableName, int rowIndex, String sqlState) {
        super("DB_IMPORT_ERROR", message, cause);
        this.tableName = tableName;
        this.rowIndex = rowIndex;
        this.sqlState = sqlState;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getSqlState() {
        return sqlState;
    }

    public void setSqlState(String sqlState) {
        this.sqlState = sqlState;
    }

    @Override
    public String toString() {
        return "DatabaseImportException{" +
                "tableName='" + tableName + '\'' +
                ", rowIndex=" + rowIndex +
                ", sqlState='" + sqlState + '\'' +
                ", errorCode='" + getErrorCode() + '\'' +
                ", errorMessage='" + getErrorMessage() + '\'' +
                '}';
    }
}
